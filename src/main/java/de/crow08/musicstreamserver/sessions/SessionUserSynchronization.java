package de.crow08.musicstreamserver.sessions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
      if(syncUsers.stream().noneMatch(syncedUser -> Objects.equals(userId, syncedUser))) {
        this.syncUsers.add(userId);
      }
    } else if (syncTargetMediaId == -1 || syncTargetDuration == -1) {
      syncTargetMediaId = mediaId;
      syncTargetDuration = duration;
      syncUsers.clear();
      System.out.println("new sync for user:" + userId);
      this.syncUsers.add(userId);
    } else {
      System.out.println("Sync conflict: ignoring new sync to: " + mediaId + " at: " + duration);
    }
  }

  public synchronized void completeSync() {
    syncUsers.clear();
    syncTargetMediaId = -1;
    syncTargetDuration = -1;
  }
}
