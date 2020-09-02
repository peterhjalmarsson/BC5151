/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bc5151;

/**
 *
 * @author peter
 */
public class CWSquarePosition {
    	public int x;
	public int y;

    public CWSquarePosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
        
    public boolean legalSquare(int width, int height){
        return (x>=0 && y>=0 && x<width && y<height);
    }
    
    public void unset(){
        x=-1;
        y=-1;
    }
}
