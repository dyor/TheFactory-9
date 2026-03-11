# Skill: KMP Project Progress Calculator

## Description
This document provides predefined instructions for an AI assistant to estimate and report how much of the project described in the `GUIDE.md` file has been completed. 

## Trigger / Usage
To use this skill in any project, ensure this file is in the `/SKILLS` directory as `CALCULATOR_SKILL.md` and prompt the AI with:
> "Calculate the project progress in accordance with SKILLS/CALCULATOR_SKILL.md and ensure that the GUIDE.md periodically invokes the CALCULATOR.md to determine how complete the project is."

---

## Instructions for the AI Assistant

When the trigger prompt is received, please perform the following steps:

### 1. Parse the `GUIDE.md` File
Locate the `GUIDE.md` file in the root directory of the project. Read its contents to understand the project phases, steps, and associated tasks.

### 2. Calculate Task Metrics
Scan through all the actionable checkboxes (e.g. `- [ ]` for incomplete, `- [x]` or `- [X]` for complete) within the `GUIDE.md` document.
Categorize the tasks by Phase and calculate:
*   **Total Tasks**: The total number of checkbox items in each phase.
*   **Completed Tasks**: The number of tasks marked as `- [x]`.
*   **Pending Tasks**: The number of tasks marked as `- [ ]`.
*   *Action Breakdown*: Count how many pending tasks are assigned to `**User Action**`, `**Agent Action**`, and `**Validation**`.

### 3. Generate `CALCULATOR.md`
Create or update a file named `CALCULATOR.md` in the root of the project with a comprehensive progress report. Populate it with the following structure:

*   **Methodology:** A brief explanation that progress is calculated based on the completion of actionable tasks (`- [x]` vs `- [ ]`) outlined in the `GUIDE.md` project phases.
*   **Phase Breakdown:** A markdown table detailing the progress of each phase. The table must have the following columns:
    *   `Phase`
    *   `Total Tasks`
    *   `Completed`
    *   `Pending`
    *   `Completion %`
*   **Actionable Insights:** A brief list of the immediate next steps (the first 1-3 pending tasks) and who is responsible (`User Action` or `Agent Action`).
*   **Overall Project Progress:** The absolute sum of all completed tasks divided by the total tasks, displayed prominently as a percentage.

### Example Output Structure
```markdown
# Project Progress Calculation

## Methodology
Progress is calculated by evaluating the completed (`- [x]`) versus pending (`- [ ]`) tasks defined across all phases in the `GUIDE.md` file.

## Phase Breakdown

| Phase | Total Tasks | Completed | Pending | Completion % |
|-------|-------------|-----------|---------|--------------|
| Phase 1: Foundation & Infrastructure | 3 | 3 | 0 | 100% |
| Phase 2: Core Features & Logic | 5 | 2 | 3 | 40% |
| Phase 3: Hardware / Native Integrations | 4 | 0 | 4 | 0% |
| Phase 4: Data Processing & Editing | 2 | 0 | 2 | 0% |
| Phase 5: Distribution & Cloud Sync | 2 | 0 | 2 | 0% |
| Phase 6: The Final Cut | 5 | 0 | 5 | 0% |

## Actionable Insights
*   **Current Phase:** Phase 2: Core Features & Logic
*   **Next Task:** `- [ ] **Agent Action**: Create core UI screens and ViewModels.`
*   **Pending Breakdown:** 1 User Action, 12 Agent Actions, 3 Validations remaining.

---

## Overall Project Progress
*   **Total Tasks**: 21
*   **Completed Tasks**: 5
*   **Pending Tasks**: 16
*   **Overall Completion**: 23.8%
```