package com.example.meetingminutesgenerator;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class HomeController {


    private final GeminiService geminiService;
    private final WhisperService whisperService;
    private final MeetingHistoryRepository meetingHistoryRepository;

    public HomeController(
            GeminiService geminiService,
            WhisperService whisperService,
            MeetingHistoryRepository meetingHistoryRepository) {

        this.geminiService = geminiService;
        this.whisperService = whisperService;
        this.meetingHistoryRepository = meetingHistoryRepository;
    }

    // ---------------- HOME ----------------
    @GetMapping("/")
    public String home(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", user.getName());

        return "index";
    }

    // ---------------- GENERATE MINUTES ----------------
    @PostMapping("/generate")
    public String generateMinutes(
            @RequestParam String title,
            @RequestParam String participants,
            @RequestParam(required = false) String transcript,
            @RequestParam(value = "audioFile", required = false) MultipartFile audioFile,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Audio upload transcription
        if ((transcript == null || transcript.isBlank())
                && audioFile != null
                && !audioFile.isEmpty()) {

            try {

                File tempFile = File.createTempFile(
                        "meeting-audio-",
                        audioFile.getOriginalFilename()
                );

                audioFile.transferTo(tempFile);

                transcript = whisperService.transcribe(tempFile);

                tempFile.delete();

            } catch (Exception e) {

                transcript = "Audio transcription failed: " + e.getMessage();

            }
        }

        if (transcript == null) {
            transcript = "";
        }

        String summary = geminiService.generateSummary(transcript);
        String actionItems = geminiService.generateActionItems(transcript);
        String keyPoints = geminiService.generateKeyPoints(transcript);

// India time (IST)
        String meetingDateTime = LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

        model.addAttribute("title", title);
        model.addAttribute("participants", participants);
        model.addAttribute("transcript", transcript);
        model.addAttribute("summary", summary);
        model.addAttribute("actionItems", actionItems);
        model.addAttribute("keyPoints", keyPoints);
        model.addAttribute("dateTime", meetingDateTime);

        MeetingHistory history = new MeetingHistory();
        history.setTitle(title);
        history.setParticipants(participants);
        history.setTranscript(transcript);
        history.setSummary(summary);
        history.setActionItems(actionItems);
        history.setKeyPoints(keyPoints);
        history.setDateTime(meetingDateTime);
        history.setCreatedAt(LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata")));
        history.setUser(user);

        meetingHistoryRepository.save(history);

        model.addAttribute("meetingId", history.getId());

        return "result";

    }

    // ---------------- DOWNLOAD CURRENT PDF ----------------
    @GetMapping("/download")
    public void downloadPDF(HttpServletResponse response,
                            HttpSession session) throws IOException {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            response.sendRedirect("/login");
            return;
        }

        List<MeetingHistory> meetings = meetingHistoryRepository.findByUser(user);

        if (meetings.isEmpty()) {
            response.sendRedirect("/history");
            return;
        }

        MeetingHistory meeting = meetings.get(meetings.size() - 1);

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=meeting-minutes.pdf"
        );

        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, response.getOutputStream());

            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headingFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);

            Paragraph title = new Paragraph("AI Meeting Minutes Generator", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Meeting Title:", headingFont));
            document.add(new Paragraph(meeting.getTitle(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Date & Time:", headingFont));
            document.add(new Paragraph(meeting.getDateTime(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Participants:", headingFont));
            document.add(new Paragraph(meeting.getParticipants(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Meeting Transcript:", headingFont));
            document.add(new Paragraph(meeting.getTranscript(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("AI Summary:", headingFont));
            document.add(new Paragraph(meeting.getSummary(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Action Items:", headingFont));
            document.add(new Paragraph(meeting.getActionItems(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Key Points:", headingFont));
            document.add(new Paragraph(meeting.getKeyPoints(), normalFont));
            document.add(new Paragraph(" "));

            Paragraph footer = new Paragraph(
                    "Generated by AI Meeting Minutes Generator",
                    normalFont
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    // ---------------- HISTORY ----------------
    @GetMapping("/history")
    public String viewHistory(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "meetings",
                meetingHistoryRepository.findByUser(user)
        );

        return "history";
    }

    // ---------------- VIEW SINGLE MEETING ----------------
    @GetMapping("/history/view/{id}")
    public String viewMeeting(@PathVariable Long id,
                              HttpSession session,
                              Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        MeetingHistory meeting =
                meetingHistoryRepository.findById(id).orElse(null);

        if (meeting == null
                || meeting.getUser() == null
                || !meeting.getUser().getId().equals(user.getId())) {

            return "redirect:/history";
        }

        model.addAttribute("meeting", meeting);

        return "view-meeting";
    }

    // ---------------- DOWNLOAD PDF FROM HISTORY ----------------
    @GetMapping("/history/download/{id}")
    public void downloadHistoryPDF(@PathVariable Long id,
                                   HttpServletResponse response,
                                   HttpSession session) throws IOException {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            response.sendRedirect("/login");
            return;
        }

        MeetingHistory meeting =
                meetingHistoryRepository.findById(id).orElse(null);

        if (meeting == null
                || meeting.getUser() == null
                || !meeting.getUser().getId().equals(user.getId())) {

            response.sendRedirect("/history");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=meeting-" + meeting.getId() + ".pdf"
        );

        try {

            Document document = new Document(
                    PageSize.A4,
                    50,
                    50,
                    50,
                    50
            );

            PdfWriter.getInstance(
                    document,
                    response.getOutputStream()
            );

            document.open();

            Font titleFont = new Font(
                    Font.FontFamily.HELVETICA,
                    18,
                    Font.BOLD
            );

            Font headingFont = new Font(
                    Font.FontFamily.HELVETICA,
                    13,
                    Font.BOLD
            );

            Font normalFont = new Font(
                    Font.FontFamily.HELVETICA,
                    11
            );

            Paragraph title = new Paragraph(
                    "AI Meeting Minutes Generator",
                    titleFont
            );

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Meeting Title:", headingFont));
            document.add(new Paragraph(meeting.getTitle(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Date & Time:", headingFont));
            document.add(new Paragraph(meeting.getDateTime(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Participants:", headingFont));
            document.add(new Paragraph(meeting.getParticipants(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Meeting Transcript:", headingFont));
            document.add(new Paragraph(meeting.getTranscript(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("AI Summary:", headingFont));
            document.add(new Paragraph(meeting.getSummary(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Action Items:", headingFont));
            document.add(new Paragraph(meeting.getActionItems(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Key Points:", headingFont));
            document.add(new Paragraph(meeting.getKeyPoints(), normalFont));
            document.add(new Paragraph(" "));

            Paragraph footer = new Paragraph(
                    "Generated by AI Meeting Minutes Generator",
                    normalFont
            );

            footer.setAlignment(Element.ALIGN_CENTER);

            document.add(footer);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    // ---------------- DELETE ----------------
    @GetMapping("/delete/{id}")
    public String deleteMeeting(@PathVariable Long id,
                                HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        MeetingHistory meeting =
                meetingHistoryRepository.findById(id).orElse(null);

        if (meeting != null
                && meeting.getUser() != null
                && meeting.getUser().getId().equals(user.getId())) {

            meetingHistoryRepository.delete(meeting);
        }

        return "redirect:/history";
    }


}
