package de.crow08.musicstreamserver.sessions;

import java.util.ArrayList;
import java.util.List;

public class SessionUserSynchronization {

  private final List<Long> syncUsers;
  private long syncTargetMediaId;
  private long syncTargetDuration;


  public SessionUserSynchronization() {
    syncUsers = new ArrayList<>();
    syncTargetMediaId = -1;
    syncTargetDuration = -1;
  }

  public List<Long> getSyncUsers() {
    return syncUsers;
  }

  public void addSyncUsers(Long userId, long mediaId, long duration) {
    if (mediaId == syncTargetMediaId && duration == syncTargetDuration) {
      this.syncUsers.add(userId);
    } else if (syncTargetMediaId == -1 || syncTargetDuration == -1) {
      syncTargetMediaId = mediaId;
      syncTargetDuration = duration;
      syncUsers.clear();
      this.syncUsers.add(userId);
    } else {
      System.out.println("Sync conflict: ignoring new sync to: " + mediaId + " at: " + duration);
    }
  }

  public void completeSync() {
    syncUsers.clear();
    syncTargetMediaId = -1;
    syncTargetDuration = -1;
  }
}
