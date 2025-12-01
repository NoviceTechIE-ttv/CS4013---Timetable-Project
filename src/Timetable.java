import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    public static void main(String args[]){
        //READ CSVS HERE
        // now that all of our data is in, we create all of our sessions
        for(int i=0;i<10;i++) {
            if (addSessions()) {
                break;
            } else {
                resetTimetable();
            }
        }
        // then we generate the master timetable
        Session[][][] masterTimetable = getMasterTimetable();
        // and read it out to a csv
        // then allow for querying
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
    public static boolean addLectures(Module module){
        int numAttempts = 10; // how many times it tries to find a suitable time and place for a session before it gives up
        // for a lecture there is only ever one group, which simplifies things a bit
        // first we try to spawn 1 session of length n where n is the number of hours of lab the module has
        boolean success = false;
        ArrayList<Session> sessions = new ArrayList<Session>();
        for(int j=0;j<numAttempts;j++) {
            Room possibleRoom = facilities.get((int) (facilities.size()*Math.random()));
            if((module.getRoomReqs()[0]==null || possibleRoom.getType().equals(module.getRoomReqs()[0]))&&module.getStudents().size()<=possibleRoom.getCapacity()){
                int[] possibleTime = possibleRoom.getAvailableTime(module.getHoursPerWeek()[0]);
                if(!checkOverlap(module.getStudents(), module.getLecturers()[0], possibleTime)){
                    // no overlaps! we've got ourselves a session!
                    success = true;
                    Session session = new Session(possibleTime[0],possibleTime[1],possibleTime[2],module.getModuleID(),"LEC",possibleRoom,module.getLecturers()[0]);
                    sessions.add(session);
                    break;
                }
            }
        }
        if(!success){
            // now we try to spawn in n sessions of length 1hr
            for(int k=0;k<module.getHoursPerWeek()[0];k++){
                for(int j=0;j<numAttempts;j++) {
                    success = false;
                    Room possibleRoom = facilities.get((int) (facilities.size()*Math.random()));
                    if((module.getRoomReqs()[0]==null || possibleRoom.getType().equals(module.getRoomReqs()[0]))&&module.getStudents().size()<=possibleRoom.getCapacity()){
                        int[] possibleTime = possibleRoom.getAvailableTime(1);
                        if(!checkOverlap(module.getStudents(), module.getLecturers()[0], possibleTime)){
                            // no overlaps! we've got ourselves a session!
                            success = true;
                            Session session = new Session(possibleTime[0],possibleTime[1],possibleTime[2],module.getModuleID(),"LEC",possibleRoom,module.getLecturers()[0]);
                            sessions.add(session);
                            break;
                        }
                    }
                }
                if(!success){
                    // we failed too many times at generating a session
                    return false;
                }
            }
        }
        // now we add that session to all of the students in it, the lecturer, and the room
        for(Session session: sessions) {
            for (Student s : module.getStudents()) {
                s.addSession(session);
            }
            module.getLecturers()[0].addSession(session);
            session.getRoom().addSession(session);
        }
        module.addLecture(new Lecture(sessions));
        // success!
        return true;
    }
    // pretty similar to addLectures but for Labs
    // but instead of adding one Lab for the Module, split the Students into n groups of m Students where
    // n = (#students in module/student cap) rounded up and m = (#students/n)
    // then do what we did with addLectures but only with the first m Students in the module's student array
    // then the next m etc etc
    // only return true after all Labs are added, if any fail after 5-10 good attempts, return false
    public static boolean addLabs(Module module){
        int numAttempts = 10; // how many times it tries to find a suitable time and place for a session before it gives up
        // first we figure out how many lecture/lab/tutorial groups there are
        int numGroups;
        int groupSize;
        if(module.getStudentCaps()[1] == -1) {
            numGroups = 1;
            groupSize = module.getStudents().size();
        }
        else{
            numGroups = (int) Math.ceil((((double) module.getStudents().size())/((double) module.getStudentCaps()[1])));
            groupSize = (int) Math.ceil(((double) module.getStudents().size()/((double) numGroups)));
        }
        for(int i=0;i<numGroups;i++){
            // for each group, we spawn some sessions
            // first we try to spawn 1 session of length n where n is the number of hours of lab the module has
            boolean success = false;
            // now we grab whatever student group it is
            ArrayList<Student> groupStudents;
            if((i+1)*groupSize <= module.getStudents().size()) {
                groupStudents = (ArrayList<Student>) module.getStudents().subList(i * groupSize, (i + 1) * groupSize);
            }
            else{
                groupStudents = (ArrayList<Student>) module.getStudents().subList(i * groupSize, module.getStudents().size());
            }
            ArrayList<Session> groupSessions = new ArrayList<Session>();
            for(int j=0;j<numAttempts;j++) {
                Room possibleRoom = facilities.get((int) (facilities.size()*Math.random()));
                
                if((module.getRoomReqs()[1]==null || possibleRoom.getType().equals(module.getRoomReqs()[1]))&&groupStudents.size()<=possibleRoom.getCapacity()){
                    int[] possibleTime = possibleRoom.getAvailableTime(module.getHoursPerWeek()[1]);
                    if(!checkOverlap(groupStudents, module.getLecturers()[1], possibleTime)){
                        // no overlaps! we've got ourselves a session!
                        success = true;
                        Session session = new Session(possibleTime[0],possibleTime[1],possibleTime[2],module.getModuleID(),"LAB",i,possibleRoom,module.getLecturers()[1]);
                        groupSessions.add(session);
                        break;
                    }
                }
            }
            if(!success){
                // now we try to spawn in n sessions of length 1hr
                for(int k=0;k<module.getHoursPerWeek()[1];k++){
                    for(int j=0;j<numAttempts;j++) {
                        success = false;
                        Room possibleRoom = facilities.get((int) (facilities.size()*Math.random()));
                        if((module.getRoomReqs()[1]==null || possibleRoom.getType().equals(module.getRoomReqs()[1]))&&groupStudents.size()<=possibleRoom.getCapacity()){
                            int[] possibleTime = possibleRoom.getAvailableTime(1);
                            if(!checkOverlap(groupStudents, module.getLecturers()[1], possibleTime)){
                                // no overlaps! we've got ourselves a session!
                                success = true;
                                Session session = new Session(possibleTime[0],possibleTime[1],possibleTime[2],module.getModuleID(),"LAB",i,possibleRoom,module.getLecturers()[1]);
                                groupSessions.add(session);
                                break;
                            }
                        }
                    }
                    if(!success){
                        // we failed too many times at generating a session
                        return false;
                    }
                }
            }
            // now we add that session to all of the students in it, the lecturer, and the room
            for(Session session: groupSessions) {
                for (Student s : groupStudents) {
                    s.addSession(session);
                }
                module.getLecturers()[1].addSession(session);
                session.getRoom().addSession(session);
            }
            module.addLab(new Lab(groupSessions));
        }
        // success!
        return true;
    }
    // almost identical to addLabs but for Tutorials
    public static boolean addTutorials(Module module){
        int numAttempts = 10; // how many times it tries to find a suitable time and place for a session before it gives up
        // first we figure out how many lecture/lab/tutorial groups there are
        int numGroups;
        int groupSize;
        if(module.getStudentCaps()[2] == -1) {
            numGroups = 1;
            groupSize = module.getStudents().size();
        }
        else{
            numGroups = (int) Math.ceil((((double) module.getStudents().size())/((double) module.getStudentCaps()[2])));
            groupSize = (int) Math.ceil(((double) module.getStudents().size()/((double) numGroups)));
        }
        for(int i=0;i<numGroups;i++){
            // for each group, we spawn some sessions
            // first we try to spawn 1 session of length n where n is the number of hours of tutorial the module has
            boolean success = false;
            // now we grab whatever student group it is
            ArrayList<Student> groupStudents;
            if((i+1)*groupSize <= module.getStudents().size()) {
                groupStudents = (ArrayList<Student>) module.getStudents().subList(i * groupSize, (i + 1) * groupSize);
            }
            else{
                groupStudents = (ArrayList<Student>) module.getStudents().subList(i * groupSize, module.getStudents().size());
            }
            ArrayList<Session> groupSessions = new ArrayList<Session>();
            for(int j=0;j<numAttempts;j++) {
                Room possibleRoom = facilities.get((int) (facilities.size()*Math.random()));
                if((module.getRoomReqs()[2]==null || possibleRoom.getType().equals(module.getRoomReqs()[2]))&&groupStudents.size()<=possibleRoom.getCapacity()){
                    int[] possibleTime = possibleRoom.getAvailableTime(module.getHoursPerWeek()[2]);
                    if(!checkOverlap(groupStudents, module.getLecturers()[2], possibleTime)){
                        // no overlaps! we've got ourselves a session!
                        success = true;
                        Session session = new Session(possibleTime[0],possibleTime[1],possibleTime[2],module.getModuleID(),"TUT",i,possibleRoom,module.getLecturers()[2]);
                        groupSessions.add(session);
                        break;
                    }
                }
            }
            if(!success){
                // now we try to spawn in n sessions of length 1hr
                for(int k=0;k<module.getHoursPerWeek()[2];k++){
                    for(int j=0;j<numAttempts;j++) {
                        success = false;
                        Room possibleRoom = facilities.get((int) (facilities.size()*Math.random()));
                        if((module.getRoomReqs()[2]==null || possibleRoom.getType().equals(module.getRoomReqs()[2]))&&groupStudents.size()<=possibleRoom.getCapacity()){
                            int[] possibleTime = possibleRoom.getAvailableTime(1);
                            if(!checkOverlap(groupStudents, module.getLecturers()[2], possibleTime)){
                                // no overlaps! we've got ourselves a session!
                                success = true;
                                Session session = new Session(possibleTime[0],possibleTime[1],possibleTime[2],module.getModuleID(),"TUT",i,possibleRoom,module.getLecturers()[2]);
                                groupSessions.add(session);
                                break;
                            }
                        }
                    }
                    if(!success){
                        // we failed too many times at generating a session
                        return false;
                    }
                }
            }
            // now we add that session to all of the students in it, the lecturer, and the room
            for(Session session: groupSessions) {
                for (Student s : groupStudents) {
                    s.addSession(session);
                }
                module.getLecturers()[2].addSession(session);
                session.getRoom().addSession(session);
            }
            module.addTutorial(new Tutorial(groupSessions));
        }
        // success!
        return true;
    }


    public static boolean checkOverlap(ArrayList<Student> students, Lecturer lecturer, int[] timeSlot){
        for(Session s: lecturer.getSessions()){
            if(s.checkOverlap(timeSlot[0],timeSlot[1],timeSlot[2])){
                // Overlap!
                return true;
            }
        }
        for(Student stu: students){
            for(Session s: lecturer.getSessions()){
                if(s.checkOverlap(timeSlot[0],timeSlot[1],timeSlot[2])){
                    // Overlap!
                    return true;
                }
            }
        }
        return false;
    }


    // deletes all Lectures, Labs, Tutorials, and Sessions in every Module, Student, Lecturer, and Room
    // so that we can populate again
    public static void resetTimetable(){
        for(Module m: bookOfModules){
            m.resetModule();
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
    public static Session[][][] getStudentTimetable(String studentID){
        Session[][][] studentTimetable = new Session[5][9][1];
        int[][] nextFreeIndex = new int[5][9];
        for(Session s: getStudentByID(studentID).getSessions()) {
            // fill every slot that that session is during
            for (int i = s.getStartTime(); i < s.getEndTime(); i++) {
                studentTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                // then increase the index
                nextFreeIndex[s.getDay()][i]++;
            }
        }
        return studentTimetable;
    }

    public static Session[][][] getRoomTimetable(String roomID){
        Session[][][] roomTimetable = new Session[5][9][1];
        int[][] nextFreeIndex = new int[5][9];
        for(Session s: getRoomByID(roomID).getSessions()) {
            // fill every slot that that session is during
            for (int i = s.getStartTime(); i < s.getEndTime(); i++) {
                roomTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]] = s;
                // then increase the index
                nextFreeIndex[s.getDay()][i]++;
            }
        }
        return roomTimetable;
    }

    // generates a timetable for a lecturer
    public static Session[][][] getLecturerTimetable(String lecturerID){
        Session[][][] lecturerTimetable = new Session[5][9][1];
        int[][] nextFreeIndex = new int[5][9];
        for(Session s: getLecturerByID(lecturerID).getSessions()){
            for(int i=s.getStartTime();i<s.getEndTime();i++){
                lecturerTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]]=s;
                // then increase the index
                nextFreeIndex[s.getDay()][i]++;
            }
        }
        return lecturerTimetable;
    }

    // generates a timetable for a module
    public static Session[][][] getModuleTimetable(String moduleID){
        Session[][][] moduleTimetable = new Session[5][9][facilities.size()];
        int[][] nextFreeIndex = new int[5][9];
        Module m = getModuleByID(moduleID);
        // get every session in the lecture
        for(Session s: m.getLecture().getSessions()){
            // fill every slot that that session is during
            for(int i=s.getStartTime();i<s.getEndTime();i++){
                moduleTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]]=s;
                // then increase the index
                nextFreeIndex[s.getDay()][i]++;
            }
        }
        // and labs and tutorials work almost exactly the same as lectures
        // except that a module can have multiple of them
        for(Lab l: m.getLabs()){
            for(Session s: l.getSessions()){
                for(int i=s.getStartTime();i<s.getEndTime();i++){
                    moduleTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]]=s;
                    nextFreeIndex[s.getDay()][i]++;
                }
            }
        }
        for(Tutorial t: m.getTutorials()){
            for(Session s: t.getSessions()){
                for(int i=s.getStartTime();i<s.getEndTime();i++){
                    moduleTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]]=s;
                    nextFreeIndex[s.getDay()][i]++;
                }
            }
        }
        return moduleTimetable;
    }

    // generates the timetable for a given year and semester of a programme
    public static Session[][][] getGroupTimetable(String programmeID, int year, int semester){
        Session[][][] groupTimetable = new Session[5][9][facilities.size()];
        // by default filled with 0s
        int[][] nextFreeIndex = new int[5][9];
        for(String modCode: getProgrammeByID(programmeID).getModulesForSemester(year, semester)){
            Module m = getModuleByID(modCode);
            // get every session in the lecture
            for(Session s: m.getLecture().getSessions()){
                // fill every slot that that session is during
                for(int i=s.getStartTime();i<s.getEndTime();i++){
                    groupTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]]=s;
                    // then increase the index
                    nextFreeIndex[s.getDay()][i]++;
                }
            }
            // and labs and tutorials work almost exactly the same as lectures
            // except that a module can have multiple of them
            for(Lab l: m.getLabs()){
                for(Session s: l.getSessions()){
                    for(int i=s.getStartTime();i<s.getEndTime();i++){
                        groupTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]]=s;
                        nextFreeIndex[s.getDay()][i]++;
                    }
                }
            }
            for(Tutorial t: m.getTutorials()){
                for(Session s: t.getSessions()){
                    for(int i=s.getStartTime();i<s.getEndTime();i++){
                        groupTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]]=s;
                        nextFreeIndex[s.getDay()][i]++;
                    }
                }
            }
        }
        return groupTimetable;
    }

    // generates the timetable for every session being offered
    public static Session[][][] getMasterTimetable(){
        // by default filled with nulls
        Session[][][] masterTimetable = new Session[5][9][facilities.size()];
        // by default filled with 0s
        int[][] nextFreeIndex = new int[5][9];
        // go through every session that is a child of a module in bookOfModules
        for(Module m: bookOfModules){
            // get every session in the lecture
            for(Session s: m.getLecture().getSessions()){
                // fill every slot that that session is during
                for(int i=s.getStartTime();i<s.getEndTime();i++){
                    masterTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]]=s;
                    // then increase the index
                    nextFreeIndex[s.getDay()][i]++;
                }
            }
            // and labs and tutorials work almost exactly the same as lectures
            // except that a module can have multiple of them
            for(Lab l: m.getLabs()){
                for(Session s: l.getSessions()){
                    for(int i=s.getStartTime();i<s.getEndTime();i++){
                        masterTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]]=s;
                        nextFreeIndex[s.getDay()][i]++;
                    }
                }
            }
            for(Tutorial t: m.getTutorials()){
                for(Session s: t.getSessions()){
                    for(int i=s.getStartTime();i<s.getEndTime();i++){
                        masterTimetable[s.getDay()][i][nextFreeIndex[s.getDay()][i]]=s;
                        nextFreeIndex[s.getDay()][i]++;
                    }
                }
            }
        }
        return masterTimetable;
    }

    // takes in a timetable and turns it into a string
    public static String toString(Session[][][] sessions){

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
