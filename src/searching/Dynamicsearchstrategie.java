package searching;

public abstract class Dynamicsearchstrategie implements SearchAdvisor {
	protected Searchdata state;
	protected final int nodetimeout;
	private long starttime;
	private int lastdistance = Integer.MAX_VALUE;

	public Dynamicsearchstrategie( int nodetimeout ) {
		this.nodetimeout = nodetimeout;
	}

	@Override
	public abstract int getMaximumPathCount();

	@Override
	public abstract int getMaximumRequestCount();

	@Override
	public int getNodeRequestTimeout() {
		return nodetimeout;
		// if (state == null || state.getRequestCount() == 0||state.getAverageLatenz()==Double.NaN)
		// return nodetimeout;
		// double percent = 5 / state.getRequestCount();
		// if (percent > 1)
		// percent = 1;
		// return (int) (percent * nodetimeout + (1 - percent)
		// * state.getAverageLatenz());
	}

	@Override
	public int getPulse() {
		int latenz = (int) state.getAverageLatenz() + 20;
		return latenz < nodetimeout ? latenz : nodetimeout;
	}

	@Override
	public abstract int getPushFactor();

	@Override
	public boolean isComplete() {
		return isTimedout();
	}

	public boolean isTimedout() {
		return System.currentTimeMillis() - starttime > nodetimeout;
	}

	@Override
	public void start( Searchdata state ) {
		this.state = state;
		starttime = System.currentTimeMillis();
	}

	@Override
	public void sync() {
		if( state.getLatestGoalDistance() != lastdistance ) {
			lastdistance = state.getLatestGoalDistance();
			starttime = System.currentTimeMillis();
		}
	}

}
