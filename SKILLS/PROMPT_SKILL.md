I want you to initialize this new KMP project by translating the skills defined in the `SKILLS/` directory into their corresponding operational files in the root directory. 

Please follow these exact steps sequentially. Do not start executing the actual development tasks yet; your goal is purely to generate the management and tracking files.

1. **Read the Skills:** 
   Read all the markdown files located in the `SKILLS/` directory (including `SKILL_SKILL.md`, `AGENTS_SKILL.md`, `VALIDATION_SKILL.md`, and `CALCULATOR_SKILL.md`, along with any others present).

2. **Generate the `SKILL.md`:**
   Execute the instructions in `SKILL_SKILL.md` to create the root-level `SKILL.md` file, which will act as the baseline for architecture rules and library versions.

3. **Generate the `GUIDE.md`:**
   Execute the instructions in `GUIDE_SKILL.md` to create the root-level `GUIDE.md` file. 
   *Here are the core details for the project you should use to populate the guide:*
   - **App Name:** Factory-9
   - **Target Audience:** KMP App Builders
   - **Core Problem:** Streamlining the creation and publication of YouTube Short educational videos about the app. 
   - **Visual Style/Theme:** Film noir-classic old school movie theme. 
   - **Core Architecture:** Kotlin Multiplatform (Android, iOS), Jetpack Compose / Compose Multiplatform, Room, Ktor, Koin, Coil, Jetpack Navigation 3, Calf permissions. 
   *Ensure the output strictly follows the Phased Execution structure and uses the `**User Action**`, `**Agent Action**`, and `**Validation**` prefixes for all tasks.*
   **Validation** before moving on to step 4, ask the user verify the contents of `GUIDE.md` and to say 'Proceed' when the review is complete. 

4. **Generate the `VALIDATION.md`:**
   Once `GUIDE.md` is created, use the instructions in `VALIDATION_SKILL.md` to generate a root-level `VALIDATION.md` file. Tailor the multi-layered testing plan specifically to the phases and features you just outlined in the `GUIDE.md`. Ask user for any information needed to create a robust validation plan. 

5. **Generate the `CALCULATOR.md`:**
   Next, use the instructions in `CALCULATOR_SKILL.md` to parse the newly created `GUIDE.md` and generate the initial `CALCULATOR.md` file to give us our starting baseline progress report. As we work through the `GUIDE.md`, we will periodically run the `CALCULATOR.md` to estimate how much of the work in `GUIDE.md` we have completed. 

6. **Generate the `AGENTS.md`:**
   Finally, use the instructions in `AGENTS_SKILL.md` to create or update an `AGENTS.md` that has agent instructions that prevent the agent from making mistakes. As we work with this project, we will continue to add more instructions to `AGENTS.md` to keep the agent working smoothly with this project.  

Once you have created all the files, let me know, and we will begin executing Phase 1 from the `GUIDE.md`.