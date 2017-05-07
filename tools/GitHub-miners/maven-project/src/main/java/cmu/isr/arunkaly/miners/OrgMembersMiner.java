package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.controller.EndPointNames;

public class OrgMembersMiner extends GenericOrgMiner{
	
	protected static String ENDPOINT_NAME = EndPointNames.ENDPOINT_ORG_MEMBERS;
	
	public OrgMembersMiner(String org,
			boolean isWriteIfCollectionExists) {
		super(org, ENDPOINT_NAME, isWriteIfCollectionExists);
	}

}
