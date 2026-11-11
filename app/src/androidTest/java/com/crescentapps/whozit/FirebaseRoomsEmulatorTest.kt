package com.crescentapps.whozit

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crescentapps.whozit.core.model.RoomRole
import com.crescentapps.whozit.core.model.Schedule
import com.crescentapps.whozit.core.model.ScheduleType
import com.crescentapps.whozit.core.model.ScheduleWithParticipants
import com.crescentapps.whozit.data.network.CreateRoomRequest
import com.crescentapps.whozit.data.network.JoinRoomRequest
import com.crescentapps.whozit.data.network.firebase.FirebaseAuthManager
import com.crescentapps.whozit.data.network.firebase.FirebaseRoomService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Firebase Emulator Integration Tests.
 *
 * To run these tests:
 * 1. Start the Firebase Emulator Suite: `firebase emulators:start --only firestore,auth`
 * 2. Ensure `google-services.json` is configured for your debug environment.
 * 3. Run this instrumented test.
 */
@RunWith(AndroidJUnit4::class)
class FirebaseRoomsEmulatorTest {

    private lateinit var authManager: FirebaseAuthManager
    private lateinit var roomService: FirebaseRoomService
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    @Before
    fun setup() {
        // Assume emulators are running locally (adjust IP if running on physical device vs AVD)
        try {
            firestore.useEmulator("10.0.2.2", 8080)
            auth.useEmulator("10.0.2.2", 9099)
        } catch (e: IllegalStateException) {
            // Already configured
        }

        authManager = FirebaseAuthManager()
        roomService = FirebaseRoomService(authManager)
        
        // Clear auth state before tests
        auth.signOut()
    }

    @Test
    fun testRoomCreationAndJoinFlow() = runBlocking {
        // Skip if firebase isn't configured in the environment at all
        if (!authManager.isFirebaseConfigured) {
            println("Skipping test: Firebase is not configured.")
            return@runBlocking
        }

        // 1. Phone A authenticates and creates a room
        val uidA = authManager.authenticateAnonymously().getOrThrow()
        
        val createRequest = CreateRoomRequest(
            name = "Test Room",
            description = "Emulator Test",
            ownerMemberId = uidA,
            ownerDisplayName = "Alice",
            initialSchedules = emptyList()
        )
        
        val createResponse = roomService.createRoom(createRequest).getOrThrow()
        val roomCode = createResponse.roomCode
        
        // 2. Verify Room Metadata exists and Owner is set
        val roomDoc = firestore.collection("rooms").document(createResponse.roomId).get().await()
        assertTrue(roomDoc.exists())
        assertEquals(uidA, roomDoc.getString("ownerMemberId"))
        
        val ownerMemberDoc = firestore.collection("rooms").document(createResponse.roomId)
            .collection("members").document(uidA).get().await()
        assertTrue(ownerMemberDoc.exists())
        assertEquals(RoomRole.OWNER.name, ownerMemberDoc.getString("role"))

        // 3. Simulate Phone B (Switch auth context)
        auth.signOut()
        val uidB = authManager.authenticateAnonymously().getOrThrow()
        
        // 4. Phone B previews room (Safe Preview)
        val preview = roomService.previewRoom(roomCode).getOrThrow()
        assertEquals("Test Room", preview.name)
        assertEquals(0, preview.members.size) // Hidden in preview
        assertEquals("Alice", preview.ownerDisplayName)

        // 5. Phone B joins room
        val joinRequest = JoinRoomRequest(
            roomCode = roomCode,
            memberId = uidB,
            memberDisplayName = "Bob"
        )
        val joinedMeta = roomService.joinRoom(joinRequest).getOrThrow()
        
        // Ensure Bob is now a member
        assertEquals(2, joinedMeta.members.size)
        val bobMember = joinedMeta.members.find { it.remoteMemberId == uidB }
        assertNotNull(bobMember)
        assertEquals(RoomRole.MEMBER, bobMember?.role)

        // 5.5 Phone A creates a shared schedule
        auth.signOut() 
        // We'll skip testing the schedule update upload with uidA, 
        // and instead test if uidB (Bob) can upload a schedule. (Because Bob is currently signed in!)
        val sched = Schedule(id = 1L, name = "Chore Test", type = ScheduleType.TASK, startDate = "2026-10-18")
        val schedWithP = ScheduleWithParticipants(schedule = sched, participants = emptyList(), pattern = emptyList())
        val uploadResult = roomService.uploadSchedule(createResponse.roomId, schedWithP, shareHistory = true)
        assertTrue(uploadResult.isSuccess)
        
        val previewAgain = roomService.previewRoom(roomCode).getOrThrow()
        assertEquals(1, previewAgain.sharedSchedules.size)
        assertEquals("Chore Test", previewAgain.sharedSchedules[0].schedule.name)

        // 6. Test Unauthorized Access Prevention (Phone C tries to delete room)
        auth.signOut()
        val uidC = authManager.authenticateAnonymously().getOrThrow()
        val deleteResult = roomService.deleteRoom(createResponse.roomId, uidC)
        assertTrue("Only owner can delete room", deleteResult.isFailure)
        
        // 7. Test Authorized Deletion with Pagination (Phone C generates a room and deletes it)
        val createRequestC = CreateRoomRequest(
            name = "Room C",
            description = "To be deleted",
            ownerMemberId = uidC,
            ownerDisplayName = "Charlie",
            initialSchedules = listOf(schedWithP, schedWithP) // Add some schedules to verify pagination delete handles subcollections
        )
        val roomC = roomService.createRoom(createRequestC).getOrThrow()
        
        // Write 1000 events to force pagination in deletion logic
        val batch1 = firestore.batch()
        for (i in 0..450) {
            batch1.set(firestore.collection("rooms").document(roomC.roomId).collection("events").document("evt_$i"), mapOf("test" to true))
        }
        batch1.commit().await()
        val batch2 = firestore.batch()
        for (i in 451..900) {
            batch2.set(firestore.collection("rooms").document(roomC.roomId).collection("events").document("evt_$i"), mapOf("test" to true))
        }
        batch2.commit().await()
        
        val delSuccess = roomService.deleteRoom(roomC.roomId, uidC)
        assertTrue("Owner should delete room", delSuccess.isSuccess)
        
        // Verify room and subcollections are gone completely
        val rDoc = firestore.collection("rooms").document(roomC.roomId).get().await()
        assertFalse(rDoc.exists())
        
        val memDocs = firestore.collection("rooms").document(roomC.roomId).collection("members").get().await()
        assertTrue(memDocs.isEmpty)
        
        val evtDocs = firestore.collection("rooms").document(roomC.roomId).collection("events").get().await()
        assertTrue(evtDocs.isEmpty)
        
        val invDoc = firestore.collection("roomInvites").document(roomC.roomCode).get().await()
        assertFalse(invDoc.exists())
    }
}
