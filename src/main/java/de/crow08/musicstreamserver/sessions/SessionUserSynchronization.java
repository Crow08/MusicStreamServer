package de.crow08.musicstreamserver.sessions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class SessionUserSynchronization {

  private final List<Long> syncUsers;
  private long syncTargetMediaId;
  private long syncTargetDuration;


  public SessionUserSynchronization() {
    syncUsers = new ArrayList<>();
    syncTargetMediaId = -1;
    syncTargetDuration = -1;
  }

  public synchronized List<Long> getSyncUsers() {
    return syncUsers;
  }

  public synchronized void addSyncUsers(Long userId, long mediaId, long duration) {
    if (mediaId == syncTargetMediaId && duration == syncTargetDuration) {
      System.out.println("Synced user:" + userId);
      if (syncUsers.stream().noneMatch(syncedUser -> Objects.equals(userId, syncedUser))) {
        this.syncUsers.add(userId);
      }
    } else if (syncTargetMediaId == -1 || syncTargetDuration == -1) {
      System.out.println("new sync for user:" + userId);
      newSyncTarget(mediaId, duration);
      Timer timer = new Timer();
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          completeSync();
        }
      }, 5000);
      this.syncUsers.add(userId);
    } else {
      System.out.println("Sync conflict: ignoring new sync to: " + mediaId + " at: " + duration);
    }
  }

  private void newSyncTarget(long mediaId, long duration) {
    syncTargetMediaId = mediaId;
    syncTargetDuration = duration;
    syncUsers.clear();
  }

  public synchronized void completeSync() {
    syncUsers.clear();
    syncTargetMediaId = -1;
    syncTargetDuration = -1;
  }
}
