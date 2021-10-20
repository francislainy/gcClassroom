import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.classroom.Classroom;
import com.google.api.services.classroom.ClassroomScopes;
import com.google.api.services.classroom.model.Course;
import com.google.api.services.classroom.model.CourseWork;
import com.google.api.services.classroom.model.Date;
import com.google.api.services.classroom.model.ListCoursesResponse;
import com.google.api.services.classroom.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ClassroomQuickstart2 {
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
        InputStream in = ClassroomQuickstart2.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Classroom service = new Classroom.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();


        // List the first 10 courses that the user has access to.
        ListCoursesResponse response = service.courses().list()
                .setPageSize(100)
                .execute();
        List<Course> courses = response.getCourses();
        if (courses == null || courses.size() == 0) {
            System.out.println("No courses found.");
        } else {
            System.out.println("Courses:");
            for (Course course : courses) {

                if (course.getCourseState().equals("ACTIVE")) {

                    TimeOfDay timeOfDay = new TimeOfDay();
                    timeOfDay.setHours(14).setMinutes(30).setSeconds(30).setNanos(10);

                    Date date = new Date();
                    date.setYear(2021).setMonth(11).setDay(23);

                    LocalDate localDate = LocalDate.now().plusDays(7);
                    java.util.Date scheduledDate = java.util.Date.from(localDate.atStartOfDay()
                            .atZone(ZoneId.systemDefault())
                            .toInstant());

                    String st = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(scheduledDate);

                    String studentUser = "fran.hmh@gmail.com";
                    IndividualStudentsOptions individualStudentsOptions = new IndividualStudentsOptions();
                    ArrayList<String> studentIdList = new ArrayList<>();
                    studentIdList.add(studentUser);
                    individualStudentsOptions.setStudentIds(studentIdList);

//                    Material material = new Material();
//                    Link link = new Link();
//                    link.setTitle("My IntelliJ link").setUrl("https://www.youtube.com/watch?v=1WwLPcVaYxY").setThumbnailUrl("https://www.youtube.com/watch?v=1WwLPcVaYxY");
//                    material.setLink(link);
//
//                    List<Material> materials = new ArrayList<>();
//                    materials.add(material);

                    CourseWork courseWork = new CourseWork()
                            .setCourseId(course.getId())
                            .setTitle("My course work")
                            .setDescription("desc")
//                            .setScheduledTime(st)
                            .setMaxPoints(100.0)
                            .setDueDate(date)
                            .setDueTime(timeOfDay)
                            .setAssigneeMode("INDIVIDUAL_STUDENTS")
                            .setIndividualStudentsOptions(individualStudentsOptions)
                            .setWorkType("ASSIGNMENT")
                            .setState("PUBLISHED");

                    courseWork = service.courses().courseWork().create(course.getId(), courseWork).execute();


//                    System.out.println(courseWork);
//
//                    courseWork
//                            .setCourseId(course.getId())
//                            .setTitle("Updated title")
//                            .setDescription("Updated description");
//
//                    courseWork = service.courses().courseWork().patch(course.getId(), courseWork.getId(), courseWork).setUpdateMask("title").setUpdateMask("description").execute();

                    ListCourseWorkResponse listCourseWorkResponse = service.courses().courseWork().list(course.getId()).execute();

                    if (listCourseWorkResponse.getCourseWork() != null) {

                        for (CourseWork courseWork1 : listCourseWorkResponse.getCourseWork()) {

                            ListStudentSubmissionsResponse listStudentSubmissionsResponse = service.courses().courseWork().studentSubmissions().list(course.getId(), courseWork1.getId()).execute();

                            for (StudentSubmission studentSubmission : listStudentSubmissionsResponse.getStudentSubmissions()) {


                                if (studentSubmission.getState().equals("TURNED_IN")) {

                                    studentSubmission.setAssignedGrade(100.0);
//                                    studentSubmission.setDraftGrade(100.0);

                                    studentSubmission = service.courses().courseWork().studentSubmissions().patch(course.getId(), courseWork.getId(), studentSubmission.getId(), studentSubmission).setUpdateMask("assignedGrade").execute();

                                }

                            }



//                            if (courseWork1.getTitle().equalsIgnoreCase("A new course work ui")) {
//
//                                courseWork1
//                                        .setCourseId(course.getId())
//                                        .setTitle("Updated title")
//                                        .setDescription("Updated description");
//
//                                courseWork1 = service.courses().courseWork().patch(course.getId(), courseWork1.getId(), courseWork1).setUpdateMask("title").setUpdateMask("description").execute();

//                            }
//                            if (courseWork1.getAssociatedWithDeveloper() != null) {
//
////                                service.courses().courseWork().delete(course.getId(), courseWork1.getId()).execute();
//                            }
                        }
                    }

                    System.out.println(listCourseWorkResponse);
                }

            }
        }

    }


}
