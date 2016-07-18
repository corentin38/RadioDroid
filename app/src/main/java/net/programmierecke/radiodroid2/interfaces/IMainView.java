package net.programmierecke.radiodroid2.interfaces;

public interface IMainView {
	void setRefreshableFragment(IFragmentRefreshable refreshable);
	void setSearchableFragment(IFragmentSearchable searchable);
	void setToolbarTitle(int titleId);
}
