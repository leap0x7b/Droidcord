package leap.droidcord;

public interface HasIcon {
	public Long getIconID();

	public String getIconHash();

	public String getIconType();

	public void iconLoaded(State s);

	public void largeIconLoaded(State s);
}
