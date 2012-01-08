package iospeci.transport;

import iospeci.OpenRange;

public class OpenIdRange implements OpenRange {
	private int startid;
	private int index = 0;
	private int count;

	public OpenIdRange( int startid , int count ) {
		this.startid = startid;
		this.count = count;
	}

	@Override
	public int popId() {
		if( index == count )
			throw new IdOutOfRangeException();
		return startid + index++;
	}

	@Override
	public int getRemainingIdCount() {
		return count - index;
	}

	@Override
	public int getTotalIdCount() {
		return count;
	}

	@Override
	public int getTransfereId() {
		return startid;
	}
}