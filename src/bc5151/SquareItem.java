package bc5151;

public abstract class SquareItem {
	public CWItemPosition position;
	SquareItem(int x, int y, int width, int height){
		position=new CWItemPosition(x,y,width,height);
	}
}
