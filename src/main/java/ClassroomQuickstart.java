import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.classroom.ClassroomScopes;
import com.google.api.services.classroom.model.*;
import com.google.api.services.classroom.Classroom;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class ClassroomQuickstart {
    private static final String APPLICATION_NAME = "Google Classroom API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";


    private static final List<String> SCOPES = Arrays.asList(
            ClassroomScopes.CLASSROOM_COURSES,
            ClassroomScopes.CLASSROOM_COURSEWORK_STUDENTS,
            ClassroomScopes.CLASSROOM_COURSEWORK_ME,
            ClassroomScopes.CLASSROOM_PROFILE_EMAILS,
            ClassroomScopes.CLASSROOM_PROFILE_PHOTOS,
            ClassroomScopes.CLASSROOM_ROSTERS);


    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";


    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = ClassroomQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        // From here we get the refresh token when we don't have the tokens folder
        return credential;
    }


    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Classroom service = new Classroom.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // List the first 10 courses that the user has access to.
        ListCoursesResponse response = service.courses().list()
                .setPageSize(10)
                .execute();
        List<Course> courses = response.getCourses();
        if (courses == null || courses.size() == 0) {
            System.out.println("No courses found.");
        } else {
            System.out.println("Courses:");
            for (Course course : courses) {
                System.out.printf("%s\n", course.getName());
            }
        }


//        Course course = new Course()
//                .setName("10th Grade Biology")
//                .setSection("Period 2")
//                .setDescriptionHeading("Welcome to 10th Grade Biology")
//                .setDescription("We'll be learning about about the structure of living creatures "
//                        + "from a combination of textbooks, guest lectures, and lab work. Expect "
//                        + "to be excited!")
//                .setRoom("301")
//                .setOwnerId("me")
//                .setCourseState("PROVISIONED");
//
//        course = service.courses().create(course).execute();
//        System.out.printf("Course created: %s (%s)\n", course.getName(), course.getId());
//
//
//        String courseId = course.getId();
//        String teacherEmail = "francislainy.campos@gmail.com";
//        Teacher teacher = new Teacher().setUserId(teacherEmail);
//        try {
//            teacher = service.courses().teachers().create(courseId, teacher).execute();
//            System.out.printf("User '%s' was added as a teacher to the course with ID '%s'.\n",
//                    teacher.getProfile().getName().getFullName(), courseId);
//        } catch (GoogleJsonResponseException e) {
//            GoogleJsonError error = e.getDetails();
//            if (error.getCode() == 409) {
//                System.out.printf("User '%s' is already a member of this course.\n", teacherEmail);
//            } else {
//                throw e;
//            }
//        }


    }
}
