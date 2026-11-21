Project Title:  PriorQ —Queue-Based Smart Task Scheduling and Deadline Prioritization System

Project Description 
 
PriorQ is a queue-based academic task scheduling and deadline prioritization system designed to help students organize and manage their academic responsibilities more efficiently. The system aims to provide students with a structured way of keeping track of their assignments, quizzes, projects, examinations, and other school-related activities. It organizes tasks based on their deadlines and priority levels, allowing students to identify which academic responsibilities need attention first.

Through the implementation of queue-based data structures, PriorQ will arrange and manage academic tasks according to their urgency and scheduled order. The system will help students monitor upcoming deadlines, keep track of task progress, and manage their study schedules in a more organized manner. By providing a centralized platform for academic task management, PriorQ aims to reduce the difficulty of handling multiple school requirements and encourage better time management, planning, and study habits.


Background of the Project

Students are often required to manage multiple academic responsibilities, including assignments, quizzes, projects, examinations, and other school-related activities. Handling these requirements simultaneously can become challenging, especially when they have different deadlines and levels of importance. Without proper planning and organization, students may struggle to determine which tasks should be prioritized, forget upcoming deadlines, or have difficulty allocating enough time for their studies. These challenges highlight the importance of having an organized approach to managing academic responsibilities.

The importance of effective time management is supported by Trentepohl et al. (2022), whose study highlighted how developing practical time management skills can help university students organize their academic activities and improve their time management abilities. Similarly, Liu et al. (2026) found a positive relationship between time management and college students' learning outcomes, suggesting that students who manage their time effectively tend to achieve better learning outcomes. These findings emphasize the value of having a structured way to plan and monitor academic responsibilities.

Furthermore, Kooren et al. (2024) discussed the relationship between procrastination and academic performance, highlighting how delays in completing academic requirements may affect students' performance. Patzak et al. (2025) also emphasized the importance of planning, goal-setting, and task prioritization as strategies that support productivity and well-being. These studies suggest that students can benefit from having a system that helps them organize their responsibilities, recognize urgent tasks, and manage their available study time.

In response to these concerns, PriorQ — Queue-Based Smart Task Scheduling and Deadline Prioritization System) is proposed as a tool specifically designed to help students manage their academic tasks more efficiently. Through the application of queue-based data structures and task prioritization techniques, the system will allow students to organize their assignments, quizzes, projects, and examinations according to their deadlines and priority levels. By providing a centralized and organized way to monitor academic responsibilities, PriorQ aims to help students identify which tasks need attention first, manage their workloads, and develop better study routines.


Problem Statement 

Students often encounter difficulties in managing their academic responsibilities due to multiple assignments, quizzes, projects, and examinations with different deadlines. Without an organized scheduling system, they may have difficulty identifying urgent tasks, deciding which academic requirements should be prioritized, and allocating sufficient time to complete each responsibility.
 
These challenges can lead to missed deadlines, disorganized study routines, and ineffective time management.

To address these concerns, PriorQ aims to answer the following questions:

1.	How can students efficiently organize and manage multiple academic tasks with different deadlines?
2.	How can the system help students identify and prioritize tasks based on their urgency and importance?
3.	How can queue-based data structures be implemented to arrange and manage academic tasks systematically?
4.	How can the system help students monitor their task progress and manage


Objectives of the Project

General Objective

The general objective of this project is to develop PriorQ, a queue-based smart task scheduling and deadline prioritization system that aims to help students organize their academic responsibilities, manage deadlines, and improve their study routines through systematic task scheduling and prioritization.

Specific Objective

Specifically, the project aims to:
1.	Develop a system that allows students to add, view, update, and manage their academic tasks, including assignments, quizzes, projects, and examinations.
2.	Implement queue-based data structures to organize and manage academic tasks according to their scheduled order and priority.
3.	Provide a task prioritization mechanism that helps students identify upcoming and urgent academic deadlines.
4.	Enable students to monitor the progress and completion status of their academic responsibilities.
5.	Provide an organized scheduling system that helps students plan their study activities and manage their academic workload more effectively.

Scope of the Project

The project focuses on developing a queue-based task scheduling and deadline prioritization system that will help students organize their academic responsibilities. It will provide a platform where students can list and manage their assignments, quizzes, projects, examinations, and other school-related activities.

The application will allow users to add tasks by entering the task title, description, deadline, and priority level. Users can also view their existing tasks, update task information, and remove tasks that are already completed or no longer needed.

The system will use Java's PriorityQueue data structure to organize tasks based on their priority levels and deadlines. This will help students identify which tasks need to be completed first. If two or more tasks have the same priority level, their deadlines will be considered, displaying the task with the nearest deadline first.

Aside from organizing tasks, the application will allow users to monitor the status of their academic responsibilities, such as pending and completed tasks. It will also use CSV file storage to save task information so users can still access their records when they open the application again.

The system will be developed using the Java programming language and Eclipse IDE. It will also apply object-oriented programming concepts and queue-based operations. The group will test its features using sample academic tasks to check whether adding, organizing, updating, and saving tasks work properly.

Overall, the project aims to provide students with a simple way to keep their academic requirements organized, become more aware of upcoming deadlines, and manage their schoolwork more effectively.

Limitations of the Project

Although the system is designed to help students manage their academic tasks, it will have some limitations.

1. The system will only focus on academic responsibilities, such as assignments, quizzes, projects, examinations, and other school-related activities. It will not include personal activities, household responsibilities, or tasks unrelated to school.

2. The system will be a standalone application that uses CSV files to store task information. Users will not be able to access or update their records across different devices because the system will not have online or cloud storage.

3. The system will not provide automatic reminders through email, text messages, or mobile notifications. Students will need to open the application themselves to check their tasks and upcoming deadlines.

4. The system will depend on the information entered by the users. They must provide accurate task details, deadlines, and priority levels. The application will organize tasks based on the information provided but will not automatically determine task difficulty or estimated completion time.

5. The system will not include advanced features such as artificial intelligence, online calendar integration, or automatic study schedule recommendations. Its main functions will be limited to organizing tasks, setting priorities, and monitoring task completion status.

Target Users

The main target users are college students, particularly the Computer Engineering students of Marinduque State University (MarSU). These students often handle different academic responsibilities, such as programming activities, laboratory exercises, assignments, quizzes, projects, and examinations. Managing these requirements can sometimes be difficult, especially when several deadlines are scheduled close to each other.

The application is intended to help students who have difficulty keeping track of their school requirements. They can use it to list their tasks, set priority levels, check upcoming deadlines, and monitor which activities have already been completed.

The system is intended for the following users:
College Students – Students who want to organize their academic requirements and keep track of their assignments, quizzes, projects, and examinations.
Computer Engineering Students of MarSU – These are the primary target users. They can use the application to manage their programming activities, laboratory exercises, projects, and other course requirements.
Students with Multiple Academic Tasks – Students who handle several school requirements at the same time and need help deciding which tasks should be completed first.

Although the project is mainly intended for Computer Engineering students of Marinduque State University, it can also be used by students from other courses who want a more organized way of managing their academic responsibilities.

Supporting Theories 

The development of this project is based on different studies and concepts related to time management, self-regulated learning, procrastination, task prioritization, and queue-based data structures. These studies explain the importance of organizing academic responsibilities and how proper planning can help students manage their schoolwork. They also provide ideas that can be applied to the development of a system that organizes tasks, monitors deadlines, and helps students keep track of their academic progress.

1. Time Management Theory

Macan et al. (1990) studied the time management habits of college students and their relationship with academic performance and stress. Their study showed that students who have better control over their time tend to feel more organized and experience less stress. It also explained that time management is not only about making schedules but also about knowing how to use available time properly.

For college students, managing time can be difficult because they usually have several academic responsibilities at once. They need to attend classes, complete assignments, prepare for quizzes and examinations, and work on different projects. Sometimes, these activities have deadlines that are close to each other, making it difficult to decide which one should be completed first. Without proper planning, students may spend too much time on less urgent activities while forgetting other important requirements.

Having a proper way to organize these responsibilities can help students become more aware of their workload. Keeping a list of tasks with their corresponding deadlines and priority levels can make it easier to identify which activities need immediate attention. This is why time management is an important concept in the development of an academic task scheduling system. It provides the idea that students should not only keep track of their requirements but also learn how to manage them based on their importance and deadlines.





2. Self-Regulated Learning Theory

Zimmerman (2002) explained that self-regulated learning is about students taking responsibility for their own learning. It involves setting goals, planning activities, monitoring progress, and checking whether their actions are helping them achieve their goals. Students who practice self-regulated learning take an active role in their studies instead of depending only on reminders or instructions from their teachers.

This concept is important for college students because they are expected to manage many of their academic responsibilities on their own. They need to know when to start an assignment, how to prepare for an examination, and how to finish their projects within the given deadlines. However, handling these activities can become challenging when students do not have a clear plan or a way to monitor their progress.

One way to practice self-regulated learning is by keeping track of academic tasks and checking their completion status. For example, a student can list all their assignments for the week, set a deadline for each one, and monitor which requirements are already finished. This makes it easier to recognize unfinished activities and decide what needs to be done next.

The same idea is applied in the proposed system through its task management and progress monitoring features. Students will be able to record their academic requirements, assign priority levels, and update their tasks once completed. These features can help them become more responsible for their schoolwork and develop better study habits. Although the system cannot guarantee better academic performance, it can provide a useful way for students to plan and monitor their own activities.



3. Queue-Based Data Structure and Priority Queue Concept

A queue is a type of data structure used to organize and manage elements. A regular queue follows the First In, First Out (FIFO) principle, which means that the first element added will also be the first one removed. However, a Priority Queue works differently because it arranges elements based on their assigned priorities. This allows elements with higher priority to be processed before those with lower priority, even if they were added later.

According to Oracle's Java documentation, the PriorityQueue class is used to store elements based on their natural ordering or a specified comparator. It is commonly used in situations where certain elements need to be processed before others. This makes it useful for applications involving scheduling and task management.

This concept is the main technical foundation of the proposed system. Instead of simply displaying academic tasks based on the order in which they were added, the program will use their priority levels and deadlines to determine their order. For example, if a student has three pending tasks with different priority levels, the system will arrange them so that the task with the highest priority comes first. If two tasks have the same priority, the one with the nearest deadline will be placed ahead of the other.

The PriorityQueue data structure will also be used in managing the different task operations. When a student adds a new requirement, the task will be inserted into the queue. When the student wants to know which task needs attention first, the system can retrieve the task at the front of the priority queue. Completed tasks can also be removed from the queue.

Using this data structure allows the program to manage tasks based on their importance instead of following only the order in which they were entered. It also demonstrates how the concepts learned in Data Structures and Algorithms can be applied to an actual problem that college students experience.

4. Procrastination and Task Prioritization

Kooren et al. (2024) conducted a study about the relationship between procrastination and academic performance. Their research discussed how delaying tasks can affect students' academic activities. It also explained the differences between active and passive procrastination and how these may have different effects on academic performance.

Procrastination is a common problem among students, especially when they have several requirements to complete. Some students tend to delay their assignments because they still have other activities to do or because they are unsure which task should come first. Others wait until the deadline is near before starting their work. This can result in rushed outputs, incomplete requirements, and difficulty managing their remaining time.

Patzak et al. (2025) also discussed different ways to improve time management, including planning, setting goals, and prioritizing tasks. These practices can help students become more organized and make better use of their available time. Knowing which activities are more important or urgent can help students avoid spending too much time on tasks that can still be completed later.

These ideas show why task prioritization is important in managing academic responsibilities. Students need to consider not only the number of tasks they have but also their deadlines and importance. For example, an assignment that is due tomorrow may need more attention than a project that is due next week. Having a system that displays these tasks according to their priority levels and deadlines can make it easier to decide what to work on first.

Through the use of task prioritization, students can have a clearer view of their pending requirements and avoid forgetting important deadlines. While organizing tasks does not completely prevent procrastination, it can help students become more aware of their responsibilities and encourage them to follow a more organized study routine.


5. Time Management and Academic Learning Outcomes

Liu et al. (2026) conducted a systematic review and meta-analysis about the relationship between time management and the learning outcomes of college students. Their research included 31 studies with a total of 13,506 participants. The findings showed a positive relationship between time management and students' learning outcomes. This suggests that students who manage their time properly may have better results in their academic activities.

College students need to divide their time between different responsibilities, such as attending classes, studying lessons, completing assignments, and preparing for examinations. When several requirements are given at the same time, it can be difficult to manage everything without proper planning. Students may forget deadlines or spend too much time on one activity, leaving less time for other requirements.

Proper task organization can help students become more aware of how many responsibilities they have and when each one needs to be completed. For example, listing assignments, quizzes, and projects together with their deadlines allows students to see which days will be more demanding. It can also help them plan their study time instead of waiting until the deadline is near.

This is one of the main ideas behind the development of an academic task scheduling system. By providing a list of recorded tasks, their deadlines, and their priority levels, students can have an easier way of checking their academic workload. They can also monitor which requirements are completed and which ones still need attention.

Overall, the study emphasizes that time management is an important part of students' academic lives. Having an organized way to schedule and monitor academic responsibilities may help them develop better study routines and become more consistent in completing their school requirements.


6. Time Management Knowledge and Practice

Trentepohl et al. (2022) conducted a study about the effects of time management knowledge and practice on students' time management skills and academic performance. Their research examined how learning and applying time management strategies can help university students improve the way they handle their academic responsibilities. The study showed that practicing time management strategies can help students improve their time management skills.

This study explains that knowing how to manage time is not always enough. Students also need to apply what they know when completing their daily activities. For example, making a list of assignments is a good start, but students also need to follow their plans, check their deadlines, and make sure that their requirements are completed on time.

In college, students often create schedules or lists of things they need to accomplish. However, keeping track of these activities can become difficult when new assignments, quizzes, and projects are added. Without regularly checking their progress, some requirements may be forgotten or left unfinished.

A task monitoring feature can make this process easier by allowing students to check their pending and completed activities in one place. They can update the status of each task after finishing it and review the remaining requirements whenever needed. This gives them a simple way to follow their plans and keep track of their progress.

The findings of this study support the importance of having practical tools for managing academic responsibilities. By combining task scheduling, priority levels, deadlines, and progress monitoring, students can have a more organized way of handling their schoolwork. These features can encourage them to apply time management practices in their daily academic routines instead of simply knowing about them.








References:

Kooren, N. S., Van Nooijen, C., & Paas, F. (2024). The influence of active and passive procrastination on academic performance: A meta-analysis. Education Sciences, 14(3), 323. https://doi.org/10.3390/educsci14030323

Liu, B., Ma, P., & Jia, F. (2026). Systematic review and meta-analysis of the impact of time management on college students' learning outcomes. Frontiers in Psychology, 17, 1700298. https://doi.org/10.3389/fpsyg.2026.1700298

Patzak, A. P., Zhang, X., & Vytasek, J. (2025). Boosting productivity and wellbeing through time management: Evidence-based strategies for higher education and workforce development. Frontiers in Education, 10. https://doi.org/10.3389/feduc.2025.1623228

Trentepohl, S., Waldeyer, J., Fleischer, J., Roelle, J., Leutner, D., & Wirth, J. (2022). How did it get so late so soon? The effects of time management knowledge and practice on students' time management skills and academic performance. Sustainability, 14(9), 5097. https://doi.org/10.3390/su14095097

