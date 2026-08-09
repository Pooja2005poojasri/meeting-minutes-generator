package com.example.meetingminutesgenerator;

import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    public String generateSummary(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            return "No transcript provided.";
        }

        String shortText = transcript.length() > 1000
                ? transcript.substring(0, 1000)
                : transcript;
        return """
Meeting Summary

The meeting discussed the main project updates, current progress, and next steps. Participants reviewed important tasks and identified the work that needs to be completed. The discussion focused on improving coordination and completing pending activities on time.

Main Decisions
- Continue the current development work.
- Track pending tasks and deadlines.
- Review progress in the next meeting.

Next Steps
- Complete assigned tasks.
- Share updates with the team.
- Prepare for the next review session.

Transcript Preview:
""" + shortText;
    }

    public String generateActionItems(String transcript) {
        return """
1. Complete the pending project tasks.
2. Update the team on progress.
3. Review deadlines and responsibilities.
4. Prepare materials for the next meeting.
""";
    }

    public String generateKeyPoints(String transcript) {
        return """
- Project progress was reviewed.
- Team discussed pending tasks.
- Responsibilities were identified.
- Deadlines were considered.
- Next meeting preparation was discussed.
""";
    }
}