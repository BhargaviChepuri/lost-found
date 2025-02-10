package com.claimit.dto;

public class ItemStatusCountDTO {
	
	private String month;
	private long totalItems;
	private long unclaimed;
	private long pendingApproval;
	private long pendingPickup;
	private long claimed;
	private long rejected;
	private long archived;

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(long totalItems) {
		this.totalItems = totalItems;
	}

	public long getUnclaimed() {
		return unclaimed;
	}

	public void setUnclaimed(long unclaimed) {
		this.unclaimed = unclaimed;
	}

	public long getPendingApproval() {
		return pendingApproval;
	}

	public void setPendingApproval(long pendingApproval) {
		this.pendingApproval = pendingApproval;
	}

	public long getPendingPickup() {
		return pendingPickup;
	}

	public void setPendingPickup(long pendingPickup) {
		this.pendingPickup = pendingPickup;
	}

	public long getClaimed() {
		return claimed;
	}

	public void setClaimed(long claimed) {
		this.claimed = claimed;
	}

	public long getRejected() {
		return rejected;
	}

	public void setRejected(long rejected) {
		this.rejected = rejected;
	}

	public long getArchived() {
		return archived;
	}

	public void setArchived(long archived) {
		this.archived = archived;
	}

}
