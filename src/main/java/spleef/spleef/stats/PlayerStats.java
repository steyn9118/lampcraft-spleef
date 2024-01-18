package spleef.spleef.stats;

import java.util.Date;

public class PlayerStats {

    private final String playerName;
    private int wins;
    private int games;
    private int blocksBroken;
    private int boosters;
    private Date lastUpdate;

    public PlayerStats(String playerName, int wins, int games, int blocksBroken, int boosters, Date lastUpdate) {
        this.playerName = playerName;
        this.wins = wins;
        this.games = games;
        this.blocksBroken = blocksBroken;
        this.boosters = boosters;
        this.lastUpdate = lastUpdate;
    }

    public int getBoosters() {
        return boosters;
    }
    public void setBoosters(int boosters) {
        this.boosters = boosters;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }
    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getWins() {
        return wins;
    }
    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getGames() {
        return games;
    }
    public void setGames(int games) {
        this.games = games;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
