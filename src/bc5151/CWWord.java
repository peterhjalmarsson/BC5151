package bc5151;


public class CWWord {
	public int length;
	public int x;
	public int y;
	public int cross;
	public boolean horizontal;
        String clue;

	CWWord(int x, int y, int length, int cross, boolean horizontal) {
		this.x = x;
		this.y = y;
		this.length = length;
		this.horizontal = horizontal;
		this.cross = cross;
                clue="";
	}

	public boolean isCrossing(CWWord w1) {
		if (horizontal == w1.horizontal)
			return false;
		if (horizontal) {
			if (x <= w1.x && x + length > w1.x && y >= w1.y
					&& y < w1.y + w1.length)
				return true;
		} else {
			if (x >= w1.x && x < w1.x + w1.length && y <= w1.y
					&& y + length > w1.y)
				return true;
		}
		return false;
	}

//	@Override
//	public int compareTo(CWWord w1) {
//		if (this.cross != w1.cross)
//			return w1.cross - this.cross;
//		else
//			return w1.length - this.length;
//	}
        
    public String getClue() {
        return clue;
    }

    public void setClue(String clue) {
        this.clue = clue;
    }
}
