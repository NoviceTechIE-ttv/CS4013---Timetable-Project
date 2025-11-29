import java.util.ArrayList;

public class Room {
    // first what we read in initially
    private String roomID;
    private String type;
    private int capacity;
    // then what we modify as we generate sessions
    private ArrayList<Session> sessions;
    private boolean[][] timeSlots; // whether the room is in use(T) or empty(F) M-F(0-4) 9a-6p(0-9)

    // constructor - called when rooms.csv is read in Tutorial
    // also sets all timeSlots to empty(F)
    public Room(String roomID, String type, int capacity){

    }

    // utility functions

    // returns a random available time slot ([startTime, endTime]) of [duration] hours
    // if none exists, return null
    // it HAS to be random not first available or the whole system breaks
    // make a list of all starting times where the next [duration] hours are available
    // then pick one at random
    public int[] getAvailableTime(int duration){

    }

    // Boring getters

    public String getRoomID(){

    }

    public String getType(){

    }

    public int getCapacity(){

    }

    public Session[] getSessions(){

    }

    // and just in case we need to reset
    // empties sessions and resets timeSlots
    public void resetRoom(){

    }

}
