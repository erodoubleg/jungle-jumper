package eren;
import java.awt.*;
public record TextObject( Vertex pos, Vertex velocity
                 , double width, double height
                 , int fontSize, String fontName, String text)
     implements GameObj{

  public TextObject( Vertex pos, String text){
    this(pos,new Vertex(0,0),0,0,15,"Arial",text);
  }


  public void paintTo(Graphics g){
    g.setColor(Color.YELLOW);
    g.setFont(new Font(fontName, Font.PLAIN, fontSize));
    g.drawString(text, (int)pos().x, (int)pos().y);
  }
}

