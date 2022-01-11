package org.sysma.schedulerExecutor;

import java.util.ArrayList;

public class FIFO extends Scheduler{
	private final ArrayList<Communication> queue;

	public FIFO(int maxWorkers) {
		super(maxWorkers);
		this.queue = new ArrayList<>();
	}

	@Override
	protected synchronized void _addWaitingJob(Communication comm) {
		queue.add(comm);
	}

	@Override
	protected synchronized Communication popJob() {
		return queue.remove(0);
	}

	@Override
	protected synchronized boolean isEmpty() {
		return queue.size() == 0;
	}

	@Override
	public void updateClass(Communication comm) {}

	/*@Override
	public double getClassWeight(Object class_) {
		return 1;
	}

	@Override
	public void setClassWeight(Object class_, double w) {
	}*/

}
