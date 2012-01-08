package searching;

public class Fireworksearchstrategie extends Dynamicsearchstrategie {
	private static final int STATE_SEEK = 0;
	private static final int STATE_EXPLODE = 1;
	private static final int STATE_STOP = 2;
	private int searchstate = STATE_SEEK;
	private final int pathlength;
	private int responsesbeforeexplode = 0;
	private final int exploderadius = 0;

	int diversity;
	int minimumdist;
	int responsecount;
	int forceSuccesInterval;

	public Fireworksearchstrategie( int nodetimeout , int pathlength , int diversity ) {
		super( nodetimeout );
		this.diversity = diversity;
		this.pathlength = pathlength;
	}

	@Override
	public int getMaximumPathCount() {
		return pathlength;
	}

	@Override
	public void start( Searchdata state ) {
		super.start( state );
		searchstate = STATE_SEEK;
		minimumdist = Integer.MAX_VALUE;
		responsecount = Integer.MAX_VALUE;
		forceSuccesInterval = getMaximumRequestCount();
	}

	@Override
	public int getMaximumRequestCount() {
		if( searchstate == STATE_EXPLODE )
			return 100;
		else
			return diversity * 2;
	}

	@Override
	public int getPushFactor() {
		if( searchstate == STATE_EXPLODE ) {
			return PUSH_ALL;
		} else
			return diversity;
	}

	@Override
	public boolean isComplete() {
		return searchstate == STATE_STOP;
	}

	@Override
	public void sync() {
		super.sync();
		updateState();

	}

	private boolean isLoop() {
		int newminimumdist = state.getMinimumGoalDistance();
		int newresponsecount = state.getResponseCount();
		if( newresponsecount > responsecount + forceSuccesInterval && newminimumdist >= minimumdist )
			return true;
		return false;
	}

	private void updateState() {
		switch ( searchstate ) {
			case STATE_SEEK: {

				if( state.isComplete() || state.getMinimumGoalDistance() == 0 ) {
					searchstate = STATE_EXPLODE;
					state.preventRequestRepitition( false );
					state.useAsPath( state.getNearestNodes().getFirst(), state.getMinimumGoalDistance() );
					state.preventRequestRepitition( true );
					state.chanceRequests();
					responsesbeforeexplode = state.getResponseCount();
				}
				break;
			}
			case STATE_EXPLODE: {
				// System.out.println(state.getResponseCount() + "-"
				// + responsesbeforeexplode + ">" + exploderadius+" complete: "+state.isComplete());
				if( state.getResponseCount() - responsesbeforeexplode > exploderadius || state.isComplete() )
					searchstate = STATE_STOP;
				break;
			}
			default :
				break;
		}
		// System.out.println("State: "+searchstate);

	}

	@Override
	public SearchAdvisor createCopy() {
		return new Fireworksearchstrategie( nodetimeout, pathlength, diversity );
	}
}
