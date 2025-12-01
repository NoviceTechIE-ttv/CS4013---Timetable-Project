import java.util.ArrayList;
import java.util.Random;

public class Room {
    // first what we read in initially
    private String roomID;
    private String type;
    private int capacity;
    // then what we modify as we generate sessions
    private ArrayList<Session> sessions;
    private boolean[][] timeSlots; // whether the room is in use(T) or empty(F) M-F(0-4) 9a-6p(0-10)

    // constructor - called when rooms.csv is read in Tutorial
    // also sets all timeSlots to empty(F)
    public Room(String roomID, String type, int capacity){
    this.roomID = roomID;
    this.type = type;
    this.capacity = capacity;
    this.sessions = new ArrayList<>();
    this.timeSlots = new boolean[5][10]; 
    }

    // utility functions

    // returns a random available time slot ([day, startTime, endTime]) of [duration] hours
    // if none exists, return null
    // it HAS to be random not first available or the whole system breaks
    // make a list of all starting times where the next [duration] hours are available
    // then pick one at random
    public int[] getAvailableTime(int duration){
        ArrayList<int[]> availableSlots = new ArrayList<>();
        
        for (int day = 0; day < 5; day++) {
            
            for (int hour = 0; hour <= 10 - duration; hour++) {
                boolean free = true;
                
                for (int h = hour; h < hour + duration; h++) {
                    if (timeSlots[day][h]) {
                        
                        free = false;
                        break;
                    }
                }
                if (free) {
                    availableSlots.add(new int[]{day, hour, hour + duration, duration});
                }
            }
        }
        
        if (availableSlots.isEmpty()) return null;
        int[] slot = availableSlots.get((int)(Math.random() * availableSlots.size()));
        return slot;
    }

    public void addSession(Session session){
        sessions.add(session);
    }

    // Boring getters

    public String getRoomID(){
        return this.roomID;
    }

    public String getType(){
        return this.type;
    }

    public int getCapacity(){
        return this.capacity;
    }

    public Session[] getSessions(){
        return sessions.toArray(new Session[0]);
    }

    public void removeSession(Session session){
    sessions.remove(session);
    }


    // and just in case we need to reset
    // empties sessions and resets timeSlots
    public void resetRoom(){
        this.sessions.clear();
        this.timeSlots = new boolean[5][10];
    }


}
