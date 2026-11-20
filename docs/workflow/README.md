# Workflow and development evidence

Use this folder as the appendix material for the project report. Fill in the templates with the team's actual names, dates, links, and work. Save screenshots or exports of the real repository history and progress board in evidence/. Do not present blank templates as proof of work.

## Repository setup

1. Choose one teammate as repository administrator. That person creates a free GitHub account if needed.
2. Create one repository using the course naming pattern [TeamName]-DSA-FinalProject, for example PriorQ-DSA-FinalProject.
3. Choose Public, or choose Private and invite the instructor and all teammates.
4. Select Add a README file and add a .gitignore for Java during creation.
5. In Settings → Collaborators (or Collaborators and teams), add teammates by GitHub username or email. Send the invitations.
6. Each teammate accepts the invitation from GitHub or the email notification.
7. Add the GitHub URL, team roles, and repository administrator to team-and-repository.md.

The ZIP contains this project's README and Java .gitignore. If creating the repository with those initialization checkboxes checked, GitHub will add its own README and .gitignore. Keep GitHub's versions, then copy the source files into the cloned repository. Review the files before committing.

## Clone and open in VS Code

Install Git, JDK 21 or later, VS Code, and the Microsoft Extension Pack for Java. In VS Code, open the Command Palette with Ctrl+Shift+P (Windows/Linux) or Cmd+Shift+P (Mac), choose Git: Clone, paste the repository URL, and select a local folder. Choose Open when VS Code prompts. Alternatively, clone from a terminal with:

    git clone https://github.com/OWNER/REPOSITORY.git
    cd REPOSITORY
    code .

Replace OWNER and REPOSITORY with the real GitHub account or organization and repository name. Do not share passwords or access tokens in the report or chat.

## Team change and commit routine

1. Before starting work, select Source Control in VS Code and use … → Pull, or run git pull --ff-only on the main branch.
2. Use … → Branch → Create Branch to make a named branch for the assigned change. Coordinate so teammates do not edit the same feature at the same time.
3. Edit and run the Java application from VS Code. Save changes.
4. In Source Control, review the changed files and the diff. Stage only the files related to the change using the + beside each file.
5. Enter a specific commit message in the required message box, such as Add deadline and priority ordering, then select Commit.
6. Select Publish Branch or Sync Changes to push the branch. Create a pull request on GitHub, ask a teammate to review it, and merge after review. If the teacher requires commits directly on main, follow the teacher's workflow and still record each real contribution.
7. Add the actual commit SHA and evidence location to commit-log.csv. Capture a screenshot of GitHub's Commits page showing author, message, and date. Capture a separate screenshot of a commit detail or pull request when needed to show each member's work.

Never make commits under another teammate's account. Each person should configure Git with their own name and GitHub email so authorship is accurate.

## Progress board

Create a GitHub Project, Trello board, or Notion board with columns To do, In progress, Review, and Done. Add one card per concrete task, assign an owner, and record a due date or milestone. Move cards as the work changes. Add the board URL to team-and-repository.md. Save dated screenshots or export the board to evidence/ at meaningful project milestones. Use progress-log.md to record the team's real weekly progress and blockers.

Suggested starting cards:

- Confirm scope, users, and requirements
- Set up Java and VS Code on each member's computer
- Create and check task priority queue
- Build add, edit, delete, and view task functions
- Add deadline, priority, status, and study time
- Check queue order and save/load behavior
- Review interface and fix issues
- Prepare demonstration and report appendix

## Evidence checklist

- Repository URL and chosen visibility
- Screenshot or export of repository Commits page with contributions from every member
- Screenshot of repository Settings → Collaborators or team access, if the instructor requests it
- Invitation acceptance or collaborator access evidence where available
- Dated screenshot/export of the progress board
- Completed weekly progress log
- Completed AI tool usage log that describes actual AI assistance and human-written work
- Final code, README, and report appendix

Save evidence screenshots as PNG or JPG with clear filenames, for example evidence/2026-11-24-github-commits.png. Do not alter screenshots in a way that changes what they prove. Do not include private email addresses or account details if a redacted view is sufficient for the instructor.
