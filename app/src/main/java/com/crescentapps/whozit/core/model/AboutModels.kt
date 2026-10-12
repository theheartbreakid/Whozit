package com.crescentapps.whozit.core.model

data class AboutInfo(
    val appName: String = "Whozit",
    val description: String = "A turn and schedule management app for organizing routines and keeping track of whose turn comes next.",
    val developerName: String = "Mohsin Mulla",
    val developerRole: String = "MCA Graduate | Aspiring Software Developer",
    val developerLocation: String = "Pune, Maharashtra, India",
    val developerIntro: String = "Hi, I'm Mohsin, a recent MCA graduate interested in programming, web technologies, and software development. I enjoy exploring technologies, building practical applications, solving problems, and learning through hands-on projects. Whozit is one of my passion projects, built to experiment with Android development, UI design, and application functionality.",
    
    val education: List<Education> = listOf(
        Education(
            degree = "Master of Computer Applications (MCA)",
            institution = "Savitribai Phule Pune University",
            years = "2024–2026",
            cgpa = "8.15/10"
        ),
        Education(
            degree = "Bachelor of Business Administration in Computer Applications (BBA-CA)",
            institution = "Savitribai Phule Pune University",
            years = "2021–2024",
            cgpa = "9.27/10"
        )
    ),
    
    val skills: Skills = Skills(
        web = listOf("HTML5", "CSS3", "JavaScript (ES6+)", "jQuery", "React.js", "Node.js", "Express.js"),
        programmingAndDb = listOf("Java", "Python", "MongoDB", "PostgreSQL", "SQL"),
        tools = listOf("Git")
    ),
    
    val projects: List<AcademicProject> = listOf(
        AcademicProject(
            name = "Campus Recruitment Drive Tracker — 2026",
            description = "A centralized web application designed to digitize campus placement activities and connect students, recruiters, and administrators.",
            technologies = "React.js, Vite, Tailwind CSS, Node.js, Express.js, MongoDB, Socket.IO, Razorpay API"
        ),
        AcademicProject(
            name = "Online Voting System — 2025",
            description = "A web application for structured voting and vote management.",
            technologies = "React.js, Node.js"
        ),
        AcademicProject(
            name = "Stock Trading Application — 2024",
            description = "A stock-trading simulation with data-handling capabilities.",
            technologies = "HTML, CSS, JavaScript, PostgreSQL, Flask"
        ),
        AcademicProject(
            name = "Movie Rating System — 2023",
            description = "A web application for rating, reviewing, and discovering movies.",
            technologies = "React.js, Node.js, Express.js, MongoDB"
        )
    ),

    val links: DeveloperLinks = DeveloperLinks(),
    
    val acknowledgements: List<Acknowledgement> = listOf(
        Acknowledgement("AndroidLiquidGlass", "Kyant and contributors", "https://github.com/Kyant0/AndroidLiquidGlass"),
        Acknowledgement("Haze", "Chris Banes and contributors", "https://github.com/chrisbanes/haze")
    ),
    
    val projectDisclosure: String = "Whozit is an independent student passion project built through AI-assisted ('vibe-coded') development, experimentation, and manual adjustments. It is a learning project that continues to evolve. Features and behavior may change as development progresses."
)

data class Education(
    val degree: String,
    val institution: String,
    val years: String,
    val cgpa: String
)

data class Skills(
    val web: List<String>,
    val programmingAndDb: List<String>,
    val tools: List<String>
)

data class AcademicProject(
    val name: String,
    val description: String,
    val technologies: String
)

data class DeveloperLinks(
    val githubProfile: String = "https://github.com/theheartbreakid",
    val whozitRepo: String = "https://github.com/theheartbreakid/Whozit",
    val releases: String = "https://github.com/theheartbreakid/Whozit/releases",
    val issues: String = "https://github.com/theheartbreakid/Whozit/issues",
    val linkedin: String = "https://www.linkedin.com/in/mohsin-mulla-99349a227/",
    val email: String = "mullamohsin48@gmail.com"
)

data class Acknowledgement(
    val libraryName: String,
    val authors: String,
    val url: String
)