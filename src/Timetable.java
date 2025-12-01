import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Timetable {
    private static ArrayList<Room> facilities;
    private static ArrayList<Programme> programmes;
    private static ArrayList<Module> bookOfModules;
    private static ArrayList<Lecturer> lecturerBody;
    private static ArrayList<Student> studentBody;

    // helper: moduleCode -> {lectureHours, tutorialHours, labHours}
    private Map<String, int[]> moduleHoursMap = new HashMap<>();

    // constructor – initialise all your lists/maps
    public Timetable() {
        facilities     = new ArrayList<>();
        programmes     = new ArrayList<>();
        bookOfModules  = new ArrayList<>();
        lecturerBody   = new ArrayList<>();
        studentBody    = new ArrayList<>();
        moduleHoursMap = new HashMap<>();
    }
    
    // first readCSVs
    // then addSessions
    // then getMasterTimetable
    // if getMasterTimetable returns false, resetTimetable and try again like 3 times
    // then allow for queries
    public static void main(String[] args) {
        Timetable timetable = new Timetable();

        // 1. Load all CSV data (rooms, programmes, modules, lecturers, students)
        timetable.readCSVs();

        // 2. Try to generate sessions (lectures, labs, tutorials)
        boolean success = false;

        for (int i = 0; i < 10; i++) {
            System.out.println("Attempt " + (i + 1) + " to generate timetable...");

            if (timetable.addSessions()) {
                success = true;
                System.out.println("Timetable successfully generated.");
                break;
            } else {
                System.out.println("Failed. Resetting timetable...");
                Timetable.resetTimetable();
            }
        }

        if (!success) {
            System.out.println("ERROR: Unable to generate a valid timetable after 10 attempts.");
        }

        // 3. Start CLI
        timetable.runCLI();
    }

    // METHODS FOR READING IN DATA

    // reads in all csvs and passes data to the functions immediately below
    // so everything ends up in the right place
    // order to read in: rooms.csv, programmes.csv, modules.csv, sessions.csv, students.csv
    public void readCSVs(){

        // (re)initialise all lists and maps in case this is called more than once
        facilities     = new ArrayList<>();
        programmes     = new ArrayList<>();
        bookOfModules  = new ArrayList<>();
        lecturerBody   = new ArrayList<>();
        studentBody    = new ArrayList<>();
        moduleHoursMap = new HashMap<>();

        // base folder for CSVs
        String basePath = "src/csv/";

        // 1. read rooms
        readRoomsCSV("rooms.csv");

        // 2. read programmes and programme->modules mapping
        readProgrammesCSV("programmes.csv", "programme_module.csv");

        // 3. read module hours and then module list
        readModuleHoursCSV("module_hours.csv");
        readModulesCSV("modules.csv");

        // 4. complete modules with lecturer / caps / room types
        readSessionsCSV("sessions.csv");

        // 5. read students and attach them to modules
        readStudentsCSV("students.csv");
    }

    // === PRIVATE HELPERS FOR CSV READING ===

    private void readRoomsCSV(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // skip header: Room,Type,Capacity

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // skip blank/comment lines
                }

                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue; // malformed line, ignore
                }

                String roomID  = parts[0].trim();
                String type    = parts[1].trim();
                int capacity   = Integer.parseInt(parts[2].trim());

                Room room = new Room(roomID, type, capacity);
                facilities.add(room);
            }
        } catch (IOException e) {
            System.out.println("Error reading " + filename);
            e.printStackTrace();
        }
    }

    private void readProgrammesCSV(String programmesFile, String programmeModulesFile) {
        ArrayList<String> programmeCodes = new ArrayList<>();

        // 1) read programme codes from programmes.csv
        try (BufferedReader br = new BufferedReader(new FileReader(programmesFile))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",");
                if (parts.length < 1) continue;

                String code = parts[0].trim();
                programmeCodes.add(code);
            }
        } catch (IOException e) {
            System.out.println("Error reading " + programmesFile);
            e.printStackTrace();
        }

        // helper inner class to store modules per year/semester
        class ModuleGrid {
            ArrayList<String>[][] grid = new ArrayList[5][3]; // [year][semester]
            ModuleGrid() {
                for (int y = 0; y < 5; y++) {
                    for (int s = 0; s < 3; s++) {
                        grid[y][s] = new ArrayList<>();
                    }
                }
            }
        }

        Map<String, ModuleGrid> progMap = new HashMap<>();
        for (String code : programmeCodes) {
            progMap.put(code, new ModuleGrid());
        }

        // 2) fill grid from programme_module.csv
        try (BufferedReader br = new BufferedReader(new FileReader(programmeModulesFile))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                String programmeID = parts[0].trim();
                int year           = Integer.parseInt(parts[1].trim());
                int semester       = Integer.parseInt(parts[2].trim());
                String moduleCode  = parts[3].trim();

                ModuleGrid mg = progMap.get(programmeID);
                if (mg != null && year >= 1 && year <= 4 && semester >= 1 && semester <= 2) {
                    mg.grid[year][semester].add(moduleCode);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading " + programmeModulesFile);
            e.printStackTrace();
        }

        // 3) build Programme objects
        for (String code : programmeCodes) {
            ModuleGrid mg = progMap.get(code);
            String[][][] modulesBySemester = new String[5][3][];
            for (int y = 1; y <= 4; y++) {
                for (int s = 1; s <= 2; s++) {
                    ArrayList<String> list = mg.grid[y][s];
                    modulesBySemester[y][s] = list.toArray(new String[0]);
                }
            }
            Programme p = new Programme(code, modulesBySemester);
            programmes.add(p);
        }
    }

    private void readModuleHoursCSV(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                String code   = parts[0].trim();
                String lecStr = parts[1].trim();
                String tutStr = parts[2].trim();
                String labStr = parts[3].trim();

                int lec = lecStr.isEmpty() ? 0 : Integer.parseInt(lecStr);
                int tut = tutStr.isEmpty() ? 0 : Integer.parseInt(tutStr);
                int lab = labStr.isEmpty() ? 0 : Integer.parseInt(labStr);

                moduleHoursMap.put(code, new int[]{lec, tut, lab});
            }
        } catch (IOException e) {
            System.out.println("Error reading " + filename);
            e.printStackTrace();
        }
    }

    private void readModulesCSV(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // header: moduleCode,moduleName
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",");
                if (parts.length < 1) continue;

                String code = parts[0].trim();
                int[] hours = moduleHoursMap.getOrDefault(code, new int[]{0, 0, 0});

                Module m = new Module(code, hours);
                bookOfModules.add(m);
            }
        } catch (IOException e) {
            System.out.println("Error reading " + filename);
            e.printStackTrace();
        }
    }

    private void readSessionsCSV(String filename) {
        // temporary maps to collect per-module data
        Map<String, Lecturer[]> lectMap = new HashMap<>();
        Map<String, int[]> capsMap      = new HashMap<>();
        Map<String, String[]> roomMap   = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String code       = parts[0].trim();
                String classType  = parts[1].trim().toLowerCase();
                String capStr     = parts[2].trim();
                String roomType   = parts[3].trim();
                String lecturerID = parts[4].trim();

                int idx;
                if (classType.equals("lecture"))      idx = 0;
                else if (classType.equals("lab"))     idx = 1;
                else if (classType.equals("tutorial"))idx = 2;
                else continue; // unknown type

                Lecturer[] la = lectMap.get(code);
                int[] caps    = capsMap.get(code);
                String[] rooms= roomMap.get(code);

                if (la == null) {
                    la    = new Lecturer[3];
                    caps  = new int[3];
                    rooms = new String[3];
                    lectMap.put(code, la);
                    capsMap.put(code, caps);
                    roomMap.put(code, rooms);
                }

                Lecturer lecturer = new Lecturer(lecturerID);
                la[idx] = lecturer;

                if (!capStr.isEmpty()) {
                    caps[idx] = Integer.parseInt(capStr);
                } else {
                    caps[idx] = 0;
                }

                rooms[idx] = roomType;
            }
        } catch (IOException e) {
            System.out.println("Error reading " + filename);
            e.printStackTrace();
        }

        // apply collected data to each Module
        for (Module m : bookOfModules) {
            String code = m.getModuleID();
            Lecturer[] la = lectMap.get(code);
            int[] caps    = capsMap.get(code);
            String[] rooms= roomMap.get(code);

            if (la != null && caps != null && rooms != null) {
                m.completeModule(la, caps, rooms);
            }
        }
    }

    private void readStudentsCSV(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                String studentID   = parts[0].trim();
                String programmeID = parts[1].trim();
                int year           = Integer.parseInt(parts[2].trim());
                int semester       = Integer.parseInt(parts[3].trim());

                addStudent(studentID, programmeID, year, semester);
            }
        } catch (IOException e) {
            System.out.println("Error reading " + filename);
            e.printStackTrace();
        }
    }
    

    public static void addRoom(){

    }

    public static void addProgramme(){

    }

    // creates a module using the information from modules.csv
    // and adds it to bookOfModules
    public static void addModule(){

    }

    // adds the information from sessions.csv to the relevant module
    public static void completeModule(){
        // if there is not a room cap for a lecture/lab/tutorial, define it as -1

    }

    // helper method for completeModule which
    private Lecturer getOrCreateLecturer(String lecturerID){
        Lecturer l = getLecturerByID(lecturerID);
        if (l == null) {
            l = new Lecturer(lecturerID);
            lecturerBody.add(l);
        }
        return l;
    }

    // adds a student to studentBody using information from students.csv
    // also adds that student to all of their modules
    public void addStudent(String studentID, String programmeID, int year, int semester){
        Student s = new Student(studentID, programmeID, year, semester);
        studentBody.add(s);

        Programme p = getProgrammeByID(programmeID);
        if (p == null) return;

        String[] moduleCodes = p.getModulesForSemester(year, semester);
        if (moduleCodes == null) return;

        for (String code : moduleCodes) {
            Module m = getModuleByID(code);
            if (m != null) {
                m.addStudent(s);
            }
        }
    }

    // METHODS FOR SEARCHING BY IDS

    public Module getModuleByID(String moduleID){
        for(Module m: bookOfModules){
            if(m.getModuleID().equals(moduleID)){
                return m;
            }
        }
        return null;
    }

    public Programme getProgrammeByID(String programmeID){
        for(Programme p: programmes){
            if(p.getProgrammeID().equals(programmeID)){
                return p;
            }
        }
        return null;
    }

    public Student getStudentByID(String studentID){
        for(Student s: studentBody){
            if(s.getStudentID().equals(studentID)){
                return s;
            }
        }
        return null;
    }

    public Lecturer getLecturerByID(String lecturerID){
        for(Lecturer l: lecturerBody){
            if(l.getLecturerID().equals(lecturerID)){
                return l;
            }
        }
        return null;
    }

    public Room getRoomByID(String roomID){
        for(Room r: facilities){
            if(r.getRoomID().equals(roomID)){
                return r;
            }
        }
        return null;
    }

    // METHODS FOR SESSION GENERATION

    // Here's the hard stuff
    // Now we have to populate all the Lecture, Lab, and Tutorial Sessions
    // and disseminate that information to the relevant Modules, Lecturers, Students, and Rooms

    // goes through every Module and creates their Lectures, Labs, and Tutorials
    // if any return false, return false
    // return true after finished adding Lectures, Labs, and Tutorials for all Modules
    public boolean addSessions(){
        for(Module m: bookOfModules){
            if(!addLectures(m) || !addLabs(m) || !addTutorials(m))
            {
                // adding one of the sessions failed and we need to restart
                return false;
            }
        }
        // we added all sessions successfully!
        return true;
    }

    // grabs the Students and relevant Lecturer from a module as well as any room reqs or student caps
    // then gets a random room, checks if they're compatible, grabs a time slot from that room
    // and sees if the time slot works for the Students and Lecturer
    // if it does, it makes a Lecture, gives it a Session, and adds the Lecture to the relevant objects
    // as outlined above and return true
    // if it doesn't, grab a new room and a new time and try again. If it doesn't work after like 5-10
    // "good" attempts (the room was an adequate capacity and type), split the x-hour long lecture into
    // x, hour long lectures and try each one individually. They don't all have to be in the same room
    public static boolean addLectures(Module module) {
        int numAttempts = 10;

        // --- safe room requirement + lecturer lookup ---
        String[] roomReqs = module.getRoomReqs();
        String lectureRoomReq = null;
        if (roomReqs != null && roomReqs.length > 0) {
            lectureRoomReq = roomReqs[0];   // may be null -> "no specific room type"
        }

        Lecturer[] lecs = module.getLecturers();
        Lecturer lectureLecturer = (lecs != null && lecs.length > 0) ? lecs[0] : null;

        boolean success = false;
        ArrayList<Session> sessions = new ArrayList<>();

        // try to spawn 1 session of length n (n = lecture hours)
        for (int j = 0; j < numAttempts; j++) {
            Room possibleRoom = facilities.get((int) (facilities.size() * Math.random()));

            if ((lectureRoomReq == null || possibleRoom.getType().equals(lectureRoomReq))
                    && module.getStudents().size() <= possibleRoom.getCapacity()) {

                int[] possibleTime = possibleRoom.getAvailableTime(module.getHoursPerWeek()[0]);

                if (!checkOverlap(module.getStudents(), lectureLecturer, possibleTime)) {
                    success = true;
                    Session session = new Session(
                            possibleTime[0], possibleTime[1], possibleTime[2],
                            module.getModuleID(), "LEC", possibleRoom, lectureLecturer
                    );
                    sessions.add(session);
                    break;
                }
            }
        }

        // if that failed, try splitting into 1-hour sessions
        if (!success) {
            for (int k = 0; k < module.getHoursPerWeek()[0]; k++) {
                for (int j = 0; j < numAttempts; j++) {
                    success = false;
                    Room possibleRoom = facilities.get((int) (facilities.size() * Math.random()));

                    if ((lectureRoomReq == null || possibleRoom.getType().equals(lectureRoomReq))
                            && module.getStudents().size() <= possibleRoom.getCapacity()) {

                        int[] possibleTime = possibleRoom.getAvailableTime(1);

                        if (!checkOverlap(module.getStudents(), lectureLecturer, possibleTime)) {
                            success = true;
                            Session session = new Session(
                                    possibleTime[0], possibleTime[1], possibleTime[2],
                                    module.getModuleID(), "LEC", possibleRoom, lectureLecturer
                            );
                            sessions.add(session);
                            break;
                        }
                    }
                }
                if (!success) {
                    return false; // failed to place a 1-hour chunk
                }
            }
        }

        // now we add that session to all of the students in it, the lecturer, and the room
        for (Session session : sessions) {
            for (Student s : module.getStudents()) {
                s.addSession(session);
            }

            if (lectureLecturer != null) {
                lectureLecturer.addSession(session);
            }

            session.getRoom().addSession(session);
        }

        module.addLecture(new Lecture(sessions));
        return true;
    }


    // pretty similar to addLectures but for Labs
    // but instead of adding one Lab for the Module, split the Students into n groups of m Students where
    // n = (#students in module/student cap) rounded up and m = (#students/n)
    // then do what we did with addLectures but only with the first m Students in the module's student array
    // then the next m etc etc
    // only return true after all Labs are added, if any fail after 5-10 good attempts, return false
    public static boolean addLabs(Module module) {
        int numAttempts = 10;

        // --- safe room requirement + lecturer lookup ---
        String[] roomReqs = module.getRoomReqs();
        String labRoomReq = null;
        if (roomReqs != null && roomReqs.length > 1) {
            labRoomReq = roomReqs[1];
        }

        Lecturer[] lecs = module.getLecturers();
        Lecturer labLecturer = (lecs != null && lecs.length > 1) ? lecs[1] : null;

        // figure out how many lab groups there are
        int totalStudents = module.getStudents().size();
        if (totalStudents == 0) {
            return true; // nothing to schedule
        }

        int[] caps = module.getStudentCaps();
        int cap = (caps != null && caps.length > 1) ? caps[1] : -1;

        int numGroups;
        int groupSize;

        if (cap <= 0) {     // -1 or 0 -> no cap, single group
            numGroups = 1;
            groupSize = totalStudents;
        } else {
            numGroups = (int) Math.ceil((double) totalStudents / (double) cap);
            groupSize = (int) Math.ceil((double) totalStudents / (double) numGroups);
        }

        for (int i = 0; i < numGroups; i++) {
            boolean success = false;

            // figure out which students are in this group safely
            int startIndex = i * groupSize;
            if (startIndex >= totalStudents) {
                continue; // no students left for this group
            }
            int endIndex = Math.min(startIndex + groupSize, totalStudents);
            ArrayList<Student> groupStudents =
                    new ArrayList<>(module.getStudents().subList(startIndex, endIndex));

            ArrayList<Session> groupSessions = new ArrayList<>();

            // try to spawn 1 session of length n (lab hours)
            for (int j = 0; j < numAttempts; j++) {
                Room possibleRoom = facilities.get((int) (facilities.size() * Math.random()));

                if ((labRoomReq == null || possibleRoom.getType().equals(labRoomReq))
                        && groupStudents.size() <= possibleRoom.getCapacity()) {

                    int[] possibleTime = possibleRoom.getAvailableTime(module.getHoursPerWeek()[1]);
                    if (!checkOverlap(groupStudents, labLecturer, possibleTime)) {
                        success = true;
                        Session session = new Session(
                                possibleTime[0], possibleTime[1], possibleTime[2],
                                module.getModuleID(), "LAB", i, possibleRoom, labLecturer
                        );
                        groupSessions.add(session);
                        break;
                    }
                }
            }

            // if that fails, try splitting into 1-hour sessions
            if (!success) {
                for (int k = 0; k < module.getHoursPerWeek()[1]; k++) {
                    for (int j = 0; j < numAttempts; j++) {
                        success = false;
                        Room possibleRoom = facilities.get((int) (facilities.size() * Math.random()));

                        if ((labRoomReq == null || possibleRoom.getType().equals(labRoomReq))
                                && groupStudents.size() <= possibleRoom.getCapacity()) {

                            int[] possibleTime = possibleRoom.getAvailableTime(1);
                            if (!checkOverlap(groupStudents, labLecturer, possibleTime)) {
                                success = true;
                                Session session = new Session(
                                        possibleTime[0], possibleTime[1], possibleTime[2],
                                        module.getModuleID(), "LAB", i, possibleRoom, labLecturer
                                );
                                groupSessions.add(session);
                                break;
                            }
                        }
                    }
                    if (!success) {
                        return false; // failed placing a 1-hour chunk
                    }
                }
            }

            // now we add those sessions to all of the students in it, the lecturer, and the room
            for (Session session : groupSessions) {
                for (Student s : groupStudents) {
                    s.addSession(session);
                }

                if (labLecturer != null) {
                    labLecturer.addSession(session);
                }

                session.getRoom().addSession(session);
            }

            module.addLab(new Lab(groupSessions));
        }

        return true;
    }


    // almost identical to addLabs but for Tutorials
    public static boolean addTutorials(Module module) {
        int numAttempts = 10;

        // --- safe room requirement + lecturer lookup ---
        String[] roomReqs = module.getRoomReqs();
        String tutRoomReq = null;
        if (roomReqs != null && roomReqs.length > 2) {
            tutRoomReq = roomReqs[2];
        }

        Lecturer[] lecs = module.getLecturers();
        Lecturer tutLecturer = (lecs != null && lecs.length > 2) ? lecs[2] : null;

        int totalStudents = module.getStudents().size();
        if (totalStudents == 0) {
            return true; // nothing to schedule
        }

        int[] caps = module.getStudentCaps();
        int cap = (caps != null && caps.length > 2) ? caps[2] : -1;

        int numGroups;
        int groupSize;

        if (cap <= 0) {     // -1 or 0 -> no cap, single group
            numGroups = 1;
            groupSize = totalStudents;
        } else {
            numGroups = (int) Math.ceil((double) totalStudents / (double) cap);
            groupSize = (int) Math.ceil((double) totalStudents / (double) numGroups);
        }

        for (int i = 0; i < numGroups; i++) {
            boolean success = false;

            // figure out which students are in this group safely
            int startIndex = i * groupSize;
            if (startIndex >= totalStudents) {
                continue; // no students left for this group
            }
            int endIndex = Math.min(startIndex + groupSize, totalStudents);
            ArrayList<Student> groupStudents =
                    new ArrayList<>(module.getStudents().subList(startIndex, endIndex));

            ArrayList<Session> groupSessions = new ArrayList<>();

            // try to spawn 1 session of length n (tutorial hours)
            for (int j = 0; j < numAttempts; j++) {
                Room possibleRoom = facilities.get((int) (facilities.size() * Math.random()));

                if ((tutRoomReq == null || possibleRoom.getType().equals(tutRoomReq))
                        && groupStudents.size() <= possibleRoom.getCapacity()) {

                    int[] possibleTime = possibleRoom.getAvailableTime(module.getHoursPerWeek()[2]);
                    if (!checkOverlap(groupStudents, tutLecturer, possibleTime)) {
                        success = true;
                        Session session = new Session(
                                possibleTime[0], possibleTime[1], possibleTime[2],
                                module.getModuleID(), "TUT", i, possibleRoom, tutLecturer
                        );
                        groupSessions.add(session);
                        break;
                    }
                }
            }

            // if that fails, try splitting into 1-hour sessions
            if (!success) {
                for (int k = 0; k < module.getHoursPerWeek()[2]; k++) {
                    for (int j = 0; j < numAttempts; j++) {
                        success = false;
                        Room possibleRoom = facilities.get((int) (facilities.size() * Math.random()));

                        if ((tutRoomReq == null || possibleRoom.getType().equals(tutRoomReq))
                                && groupStudents.size() <= possibleRoom.getCapacity()) {

                            int[] possibleTime = possibleRoom.getAvailableTime(1);
                            if (!checkOverlap(groupStudents, tutLecturer, possibleTime)) {
                                success = true;
                                Session session = new Session(
                                        possibleTime[0], possibleTime[1], possibleTime[2],
                                        module.getModuleID(), "TUT", i, possibleRoom, tutLecturer
                                );
                                groupSessions.add(session);
                                break;
                            }
                        }
                    }
                    if (!success) {
                        return false; // failed placing a 1-hour chunk
                    }
                }
            }

            // now we add those sessions to all of the students in it, the lecturer, and the room
            for (Session session : groupSessions) {
                for (Student s : groupStudents) {
                    s.addSession(session);
                }

                if (tutLecturer != null) {
                    tutLecturer.addSession(session);
                }

                session.getRoom().addSession(session);
            }

            module.addTutorial(new Tutorial(groupSessions));
        }

        return true;
    }



    public static boolean checkOverlap(ArrayList<Student> students, Lecturer lecturer, int[] timeSlot){
        if (timeSlot == null) {
            // no slot was found – treat as conflict so caller can retry
            return true;
        }

        // Check lecturer clashes (if a lecturer is assigned)
        if (lecturer != null) {
            for (Session s : lecturer.getSessions()) {
                if (s.checkOverlap(timeSlot[0], timeSlot[1], timeSlot[2])) {
                    return true;
                }
            }
        }

        // Check each student's existing sessions
        for (Student stu : students) {
            for (Session s : stu.getSessions()) {
                if (s.checkOverlap(timeSlot[0], timeSlot[1], timeSlot[2])) {
                    return true;
                }
            }
        }

        return false;
    }



    // deletes all Lectures, Labs, Tutorials, and Sessions in every Module, Student, Lecturer, and Room
    // so that we can populate again
    public static void resetTimetable(){
        for (Module m : bookOfModules) {
            m.resetModule();
        }

        // clear sessions from students
        for (Student s : studentBody) {
            s.resetStudent();
        }

        // clear sessions from lecturers
        for (Lecturer l : lecturerBody) {
            l.resetLecturer();
        }

        // clear sessions + timeslots from rooms
        for (Room r : facilities) {
            r.resetRoom();
        }
    }


    // METHODS FOR ANSWERING QUERIES AND GENERATING TIMETABLES
    // they all take in a three dimensional Session array
    // these represent the day and then the hour and then a list of all the sessions happening then
    // even though a Student and a Lecturer only have at most 1 session in any given time slot
    // we still use an array of length 1 instead of a Session for consistency
    // because modules, groups, and the master timetable can have sessions simultaneously
    // we give them a size of facilities.size() because
    // there will never be more sessions simultaneously than there are rooms in facilities
    // but there could be exactly that many if everything filled up

    // generates a timetable for a student
    public Session[][][] getStudentTimetable(String studentID){
        Session[][][] studentTimetable = new Session[5][10][1];   // 10 hours
        int[][] nextFreeIndex = new int[5][10];

        Student stu = getStudentByID(studentID);
        if (stu == null) return studentTimetable;

        for (Session s : stu.getSessions()) {
            for (int i = s.getStartTime(); i < s.getEndTime(); i++) {
                studentTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                nextFreeIndex[s.getDay()][i]++;
            }
        }
        return studentTimetable;
    }


    public Session[][][] getRoomTimetable(String roomID){
        Session[][][] roomTimetable = new Session[5][10][1];
        int[][] nextFreeIndex = new int[5][10];

        Room room = getRoomByID(roomID);
        if (room == null) return roomTimetable;

        for (Session s : room.getSessions()) {
            for (int i = s.getStartTime(); i < s.getEndTime(); i++) {
                roomTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                nextFreeIndex[s.getDay()][i]++;
            }
        }
        return roomTimetable;
    }


    // generates a timetable for a lecturer
    public Session[][][] getLecturerTimetable(String lecturerID){
        Session[][][] lecturerTimetable = new Session[5][10][1];
        int[][] nextFreeIndex = new int[5][10];

        Lecturer lec = getLecturerByID(lecturerID);
        if (lec == null) return lecturerTimetable;

        for (Session s : lec.getSessions()){
            for (int i = s.getStartTime(); i < s.getEndTime(); i++){
                lecturerTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                nextFreeIndex[s.getDay()][i]++;
            }
        }
        return lecturerTimetable;
    }

    // generates a timetable for a module
    public Session[][][] getModuleTimetable(String moduleID){
        Session[][][] moduleTimetable = new Session[5][10][facilities.size()];
        int[][] nextFreeIndex = new int[5][10];

        Module m = getModuleByID(moduleID);
        if (m == null) return moduleTimetable;

        for (Session s : m.getLecture().getSessions()){
            for (int i = s.getStartTime(); i < s.getEndTime(); i++){
                moduleTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                nextFreeIndex[s.getDay()][i]++;
            }
        }
        for (Lab l : m.getLabs()){
            for (Session s : l.getSessions()){
                for (int i = s.getStartTime(); i < s.getEndTime(); i++){
                    moduleTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                    nextFreeIndex[s.getDay()][i]++;
                }
            }
        }
        for (Tutorial t : m.getTutorials()){
            for (Session s : t.getSessions()){
                for (int i = s.getStartTime(); i < s.getEndTime(); i++){
                    moduleTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                    nextFreeIndex[s.getDay()][i]++;
                }
            }
        }
        return moduleTimetable;
    }


    // generates the timetable for a given year and semester of a programme
    public Session[][][] getGroupTimetable(String programmeID, int year, int semester){
        Session[][][] groupTimetable = new Session[5][10][facilities.size()];
        int[][] nextFreeIndex = new int[5][10];

        Programme p = getProgrammeByID(programmeID);
        if (p == null) return groupTimetable;

        String[] mods = p.getModulesForSemester(year, semester);
        if (mods == null) return groupTimetable;

        for (String modCode : mods){
            Module m = getModuleByID(modCode);
            if (m == null) continue;

            for (Session s : m.getLecture().getSessions()){
                for (int i = s.getStartTime(); i < s.getEndTime(); i++){
                    groupTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                    nextFreeIndex[s.getDay()][i]++;
                }
            }
            for (Lab l : m.getLabs()){
                for (Session s : l.getSessions()){
                    for (int i = s.getStartTime(); i < s.getEndTime(); i++){
                        groupTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                        nextFreeIndex[s.getDay()][i]++;
                    }
                }
            }
            for (Tutorial t : m.getTutorials()){
                for (Session s : t.getSessions()){
                    for (int i = s.getStartTime(); i < s.getEndTime(); i++){
                        groupTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                        nextFreeIndex[s.getDay()][i]++;
                    }
                }
            }
        }
        return groupTimetable;
    }


    // generates the timetable for every session being offered
    public static Session[][][] getMasterTimetable(){
        Session[][][] masterTimetable = new Session[5][10][facilities.size()];
        int[][] nextFreeIndex = new int[5][10];

        for (Module m : bookOfModules){
            for (Session s : m.getLecture().getSessions()){
                for (int i = s.getStartTime(); i < s.getEndTime(); i++){
                    masterTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                    nextFreeIndex[s.getDay()][i]++;
                }
            }
            for (Lab l : m.getLabs()){
                for (Session s : l.getSessions()){
                    for (int i = s.getStartTime(); i < s.getEndTime(); i++){
                        masterTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                        nextFreeIndex[s.getDay()][i]++;
                    }
                }
            }
            for (Tutorial t : m.getTutorials()){
                for (Session s : t.getSessions()){
                    for (int i = s.getStartTime(); i < s.getEndTime(); i++){
                        masterTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                        nextFreeIndex[s.getDay()][i]++;
                    }
                }
            }
        }
        return masterTimetable;
    }


    //writing a new CSV as the final timetable
    public void writeMasterTimetableCSV(String filename, Session[][][] master){
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("day,startHour,endHour,moduleCode,type,room,lecturer\n");
            for (int day = 0; day < 5; day++) {
                for (int hour = 0; hour < 10; hour++) {
                    for (int k = 0; k < master[day][hour].length; k++) {
                        Session s = master[day][hour][k];
                        if (s == null) continue;
                        fw.write(day + "," +
                                s.getStartTime() + "," +
                                s.getEndTime() + "," +
                                s.getModuleCode() + "," +
                                s.getType() + "," +
                                s.getRoom().getRoomID() + "," +
                                s.getLecturer().getLecturerID() + "\n");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error writing master timetable CSV: " + filename);
            e.printStackTrace();
        }
    }

    public void runCLI() {
    Scanner scanner = new Scanner(System.in);
    boolean running = true;

    while (running) {
        System.out.println("====== UL Timetabling System ======");
        System.out.println("1. Login as Student");
        System.out.println("2. Login as Lecturer");
        System.out.println("3. Login as Admin");
        System.out.println("0. Exit");
        System.out.print("Select option: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> studentMenu(scanner);
            case "2" -> lecturerMenu(scanner);
            case "3" -> adminMenu(scanner);
            case "0" -> running = false;
            default -> System.out.println("Invalid choice.");
        }
    }

    System.out.println("Goodbye.");
    scanner.close();
}

    private void studentMenu(Scanner scanner) {
        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine().trim();

        boolean back = false;
        while (!back) {
            System.out.println("\n--- Student Menu ---");
            System.out.println("1. View my timetable");
            System.out.println("2. View module schedule");
            System.out.println("0. Back");
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    Session[][][] tt = getStudentTimetable(studentId);
                    printTimetable(tt);
                }
                case "2" -> {
                    System.out.print("Enter Module Code: ");
                    String moduleCode = scanner.nextLine().trim();
                    Session[][][] tt = getModuleTimetable(moduleCode);
                    printTimetable(tt);
                }
                case "0" -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void lecturerMenu(Scanner scanner) {
        System.out.print("Enter Lecturer ID: ");
        String lecturerId = scanner.nextLine().trim();

        boolean back = false;
        while (!back) {
            System.out.println("\n--- Lecturer Menu ---");
            System.out.println("1. View my timetable");
            System.out.println("2. View room schedule");
            System.out.println("0. Back");
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    Session[][][] tt = getLecturerTimetable(lecturerId);
                    printTimetable(tt);
                }
                case "2" -> {
                    System.out.print("Enter Room ID: ");
                    String roomId = scanner.nextLine().trim();
                    Session[][][] tt = getRoomTimetable(roomId);
                    printTimetable(tt);
                }
                case "0" -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void adminMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View programme year/semester timetable");
            System.out.println("2. View module schedule");
            System.out.println("3. View room schedule");
            System.out.println("4. View lecturer timetable");
            System.out.println("5. Edit a module session");
            System.out.println("0. Back");
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.print("Programme ID: ");
                    String progId = scanner.nextLine().trim();
                    System.out.print("Year (1-4): ");
                    int year = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Semester (1-2): ");
                    int sem = Integer.parseInt(scanner.nextLine().trim());
                    Session[][][] tt = getGroupTimetable(progId, year, sem);
                    printTimetable(tt);
                }
                case "2" -> {
                    System.out.print("Module Code: ");
                    String moduleCode = scanner.nextLine().trim();
                    Session[][][] tt = getModuleTimetable(moduleCode);
                    printTimetable(tt);
                }
                case "3" -> {
                    System.out.print("Room ID: ");
                    String roomId = scanner.nextLine().trim();
                    Session[][][] tt = getRoomTimetable(roomId);
                    printTimetable(tt);
                }
                case "4" -> {
                    System.out.print("Lecturer ID: ");
                    String lecturerId = scanner.nextLine().trim();
                    Session[][][] tt = getLecturerTimetable(lecturerId);
                    printTimetable(tt);
                }
                case "5" -> adminEditSession(scanner);   // <-- new
                case "0" -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void adminEditSession(Scanner scanner) {
        System.out.print("Enter Module Code: ");
        String moduleCode = scanner.nextLine().trim();

        Module m = getModuleByID(moduleCode);
        if (m == null) {
            System.out.println("Module not found.");
            return;
        }

        // collect all sessions for this module
        ArrayList<Session> allSessions = new ArrayList<>();

        if (m.getLecture() != null) {
            allSessions.addAll(m.getLecture().getSessions());
        }
        for (Lab lab : m.getLabs()) {
            allSessions.addAll(lab.getSessions());
        }
        for (Tutorial tut : m.getTutorials()) {
            allSessions.addAll(tut.getSessions());
        }

        if (allSessions.isEmpty()) {
            System.out.println("No sessions found for this module.");
            return;
        }

        // list them
        System.out.println("\nSessions for " + moduleCode + ":");
        for (int i = 0; i < allSessions.size(); i++) {
            Session s = allSessions.get(i);
            int displayStart = 9 + s.getStartTime();
            int displayEnd   = 9 + s.getEndTime();
            String type = s.getType();
            String roomId = (s.getRoom() != null) ? s.getRoom().getRoomID() : "N/A";
            int group = s.getGroup();

            System.out.printf(
                    "%d) %s grp %d | day %d | %02d:00-%02d:00 | room %s%n",
                    i + 1, type, group, s.getDay(), displayStart, displayEnd, roomId
            );
        }

        System.out.print("Select session number to edit: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }

        if (choice < 0 || choice >= allSessions.size()) {
            System.out.println("Invalid session selection.");
            return;
        }

        Session session = allSessions.get(choice);

        System.out.println("Leave input blank to keep current value.");

        // day (0-4)
        System.out.print("New day (0=Mon ... 4=Fri), current " + session.getDay() + ": ");
        String dayStr = scanner.nextLine().trim();
        if (!dayStr.isEmpty()) {
            int newDay = Integer.parseInt(dayStr);
            session.setDay(newDay);
        }

        // times in real hours, convert to slots
        int currentStartHour = 9 + session.getStartTime();
        int currentEndHour   = 9 + session.getEndTime();

        System.out.print("New start hour (9-18), current " + currentStartHour + ": ");
        String startStr = scanner.nextLine().trim();
        System.out.print("New end hour (10-19), current " + currentEndHour + ": ");
        String endStr = scanner.nextLine().trim();

        if (!startStr.isEmpty() && !endStr.isEmpty()) {
            int newStartHour = Integer.parseInt(startStr);
            int newEndHour   = Integer.parseInt(endStr);
            int newStartSlot = newStartHour - 9;
            int newEndSlot   = newEndHour - 9;
            session.setStartTime(newStartSlot);
            session.setEndTime(newEndSlot);
        }

        // room
        String currentRoomId = (session.getRoom() != null) ? session.getRoom().getRoomID() : "N/A";
        System.out.print("New room ID, current " + currentRoomId + ": ");
        String newRoomId = scanner.nextLine().trim();
        if (!newRoomId.isEmpty()) {
            Room newRoom = getRoomByID(newRoomId);
            if (newRoom == null) {
                System.out.println("Room not found, room not changed.");
            } else {
                Room oldRoom = session.getRoom();
                if (oldRoom != null) {
                    oldRoom.removeSession(session);
                }
                newRoom.addSession(session);
                session.setRoom(newRoom);
            }
        }

        System.out.println("Session updated.");
    }


    private void printTimetable(Session[][][] table) {
        if (table == null) {
            System.out.println("No timetable available.");
            return;
        }

        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri"};

        for (int day = 0; day < table.length; day++) {
            System.out.println("=== " + dayNames[day] + " ===");
            for (int hour = 0; hour < table[day].length; hour++) {
                boolean any = false;
                StringBuilder sb = new StringBuilder();

                for (int k = 0; k < table[day][hour].length; k++) {
                    Session s = table[day][hour][k];
                    if (s != null) {
                        any = true;
                        int displayStart = 9 + s.getStartTime();
                        int displayEnd   = 9 + s.getEndTime();

                        Room room = s.getRoom();
                        String roomId = (room != null) ? room.getRoomID() : "N/A";

                        Lecturer lec = s.getLecturer();
                        String lecId = (lec != null) ? lec.getLecturerID() : "N/A";

                        sb.append(String.format(
                                "[%02d:00-%02d:00] %s %s (grp %d) Room:%s Lec:%s  ",
                                displayStart,
                                displayEnd,
                                s.getModuleCode(),
                                s.getType(),
                                s.getGroup(),
                                roomId,
                                lecId
                        ));
                    }
                }

                if (any) {
                    System.out.println("Slot " + hour + ": " + sb);
                }
            }
            System.out.println();
        }
    }
}



// ORDER IS ALWAYS LECTURE, LAB, TUTORIAL
// we will assume for the moment that every module has >= 1hr/wk of lectures, labs, and tutorials

// What exactly are Lectures, Labs, Tutorials, and Sessions?
// A Session is an instance of a Lecture, Lab, or Tutorial, with a specific room and a start and end time
// For example, we have a weekly OOD Lecture on Wednesdays at 11-12 in CSG001. This would be a session.
// The Lecture in this case would include both the session on Wednesdays in CSG001 and the session in ERB on Thursdays
// Labs and Tutorials are a bit more complicated - much like Lectures they can have multiple sessions
// But a Module will also often have multiple Labs and multiple Tutorials
// These are easiest to equate with Lab "groups", i.e. if you are in Lab A then you will be with the same
// subset of the class for both sessions of that lab and those in Lab B will have completely different sessions
// with different students at a different time and likely in a different room
// All of this to say, it is important to distinguish between Lecture/Lab/Tutorial objects and Session objects
// Even if a Lecture only has one Session
