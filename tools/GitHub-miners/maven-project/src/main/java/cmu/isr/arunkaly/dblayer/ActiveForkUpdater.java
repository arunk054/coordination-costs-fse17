package cmu.isr.arunkaly.dblayer;

import java.util.List;

import cmu.isr.arunkaly.controller.ActivityNames;

public class ActiveForkUpdater implements ActivityUpdater {

	public int getCount(GithubUser gu) {
		List<Activity> activities = gu.getActivities();
		int count = 0;
		List<Activity> createdActivities = gu.acquireMatchingActivities(new String[]{ActivityNames.FORK_CREATED});
		List<Activity> pushedActivities = gu.acquireMatchingActivities(new String[]{ActivityNames.FORK_PUSHED});

		//not forked at all
		if (createdActivities.size() == 0)
			return 0;
		int len = createdActivities.size();
		//anamoly
		if (len > pushedActivities.size())
			len = pushedActivities.size();
		
		for (int i = 0; i < len; ++i) {
			Activity created = createdActivities.get(i);
			Activity pushed =  pushedActivities.get(i);
			try {
				if (pushed.getDate().after(created.getDate()))
					count++;
			} catch (NullPointerException e) {
			}
		}
		return count;
	}

}
