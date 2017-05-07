package cmu.isr.arunkaly.pypi.issues;

import cmu.isr.arunkaly.dblayer.DBInterface;

public interface DBInterfaceStateful extends DBInterface {

	public void setIsWriteEnabled(boolean b);
}
