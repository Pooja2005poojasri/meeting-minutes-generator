package com.example.meetingminutesgenerator;

import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    public String generateSummary(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            return "No transcript provided.";
        }

        String shortText = transcript.length() > 300
                ? transcript.substring(0, 300)
                : transcript;

        return "Meeting Summary\\n\\n" +
                "The meeting discussed the main project updates, current progress, and next steps. " +
                "Participants reviewed important tasks and identified the work that needs to be completed. " +
                "The discussion focused on improving coordination and completing pending activities on time.\\n\\n" +
                "Main Decisions\\n" +
                "- Continue the current development work.\\n" +
                "- Track pending tasks and deadlines.\\n" +
                "- Review progress in the next meeting.\\n\\n" +
                "Next Steps\\n" +
                "- Complete assigned tasks.\\n" +
                "- Share updates with the team.\\n" +
                "- Prepare for the next review session.\\n\\n" +
                "Transcript Preview:\\n" + shortText;
    }

    public String generateActionItems(String transcript) {
        return "1. Complete the pending project tasks.\\n" +
                "2. Update the team on progress.\\n" +
                "3. Review deadlines and responsibilities.\\n" +
                "4. Prepare materials for the next meeting.";
    }

    public String generateKeyPoints(String transcript) {
        return "- Project progress was reviewed.\\n" +
                "- Team discussed pending tasks.\\n" +
                "- Responsibilities were identified.\\n" +
                "- Deadlines were considered.\\n" +
                "- Next meeting preparation was discussed.";
    }
}