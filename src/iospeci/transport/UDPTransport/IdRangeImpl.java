package iospeci.transport.UDPTransport;

import iospeci.IdRange;

class IdRangeImpl implements IdRange {
	private final int transferid;
	private final int count;

	public IdRangeImpl( final int transferid , final int count ) {
		this.transferid = transferid;
		this.count = count;
	}

	@Override
	public int getTotalIdCount() {
		return count;
	}

	@Override
	public int getTransfereId() {
		return transferid;
	}

}