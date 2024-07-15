package eren.client;
import java.awt.*;
import java.util.List;

import eren.*;
import eren.ImageObject;
import eren.GameObj;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.*;
import java.util.Random;
import java.io.*;
import static java.awt.event.KeyEvent.*;
import javax.sound.sampled.*;


class JungleJumper implements Game {
  private FallingImage player;
  static String runningpic = "eren/img/running.gif";
  static String jumpingpic = "eren/img/jumping.png";
  static String nodamagepic = "eren/img/nodamage.png";
  static String damagepic = "eren/img/damage.png";
  private Clip music, jumpingsound, lostsound, damagesound;
  private boolean hittree = false;

  private final List<List<? extends GameObj>> goss;
  private final int width, height;
  private final int[] health, score;
  private final List<GameObj> hintergrund, banana, trees, texthealth, textscore;


  JungleJumper() {
    this.player = new FallingImage(runningpic, new Vertex(100, 285), new Vertex(0, 0));
    this.goss = new ArrayList<>();
    this.width = 960;
    this.height = 400;
    this.health = new int[]{0};
    this.score = new int[]{0};
    this.trees = new ArrayList<>();
    this.banana = new ArrayList<>();
    this.texthealth = new ArrayList<>();
    this.textscore = new ArrayList<>();
    this.hintergrund = new ArrayList<>();
  }

  public void init(){
    health[0] = 0;
    initBackgroundMusic();
    initJumpingSound();
    initLostSound();
    initDamageSound();
    goss().clear();
    goss().add(hintergrund);
    goss().add(banana);
    goss().add(trees);
    goss().add(texthealth);
    goss().add(textscore);
    hintergrund.clear();
    banana.clear();
    trees.clear();
    texthealth.clear();
    textscore.clear();

    int numBackgrounds = (width() / 3840) + 2;
    for (int i = 0; i < numBackgrounds; i++) {
      hintergrund.add(
              new ImageObject(
                      new Vertex(i * 3840, 0),
                      new Vertex(-3, 0),
                      "eren/img/jungle.jpg"));
    }

    int maxTrees = 2;
    for (int i = 0; i < maxTrees; i++) {
      trees.add(
              new ImageObject(
                      new Vertex(800 + i * 500, 320),
                      new Vertex(-3, 0),
                      i % 2 == 0 ? "eren/img/tree3.png" : "eren/img/tree5.png"));
    }

    Random zufall = new Random();
    int value = zufall.nextInt(200);
    banana.add(
            new ImageObject(
                    new Vertex(800+value,180),
                    new Vertex(-1,0),
                    "eren/img/banana2.png"));
    banana.add(
            new ImageObject(
                    new Vertex(800+value,250),
                    new Vertex(-1.5,0),
                    "eren/img/banana2.png"));
    texthealth.add(
            new TextObject(
                    new Vertex(850,30),
                    "Health: 0"));
    textscore.add(
            new TextObject(
                    new Vertex(850,50),
                    "Score: 0"));

  }

  public void doChecks() {
    if (player.pos().y > 290) {
      player.velocity().y = 0;
      player = changeAnimation(runningpic, player.pos(), player.velocity());
    }

    if (health[0] < 0){
      music.stop();
      damagesound.stop();
      if (lostsound.isRunning()) {
        lostsound.stop();
      }
      lostsound.setFramePosition(0);
      lostsound.start();
      int result = JOptionPane.showConfirmDialog(
              null,
              "Dein Score: " + score[0] + "\n" + "Drücke OK, für ein neues Spiel.",
              "Game Over", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        init();
      } else {
        System.exit(0);
      }
    }

    for (var w : trees) {
      if (w.pos().x < -100) {
        w.pos().x = width();
        hittree = false;
      }
      if (player.touches(w) && !hittree) {
        w.pos().moveTo(new Vertex(width() + 10, w.pos().y));
        texthealth.clear();
        health[0]--;
        texthealth.add(new TextObject(new Vertex(850, 30), "Health: " + health[0]));
        hittree = true;
        if (damagesound.isRunning()) {
          damagesound.stop();
        }
        damagesound.setFramePosition(0);
        damagesound.start();
      }
    }

    for (var g : banana) {
      if (g.isLeftOf(0)) {
        g.pos().x = width();
      }
      if (player.touches(g)) {
        g.pos().moveTo(new Vertex(width() + 10, g.pos().y));
        textscore.clear();
        texthealth.clear();
        health[0]++;
        score[0]++;
        texthealth.add(new TextObject(new Vertex(850, 30), "Health: " + health[0]));
        textscore.add(new TextObject(new Vertex(850, 50), "Score: " + score[0]));
      }
    }
  }

  public void keyPressedReaction(KeyEvent keyEvent){
    switch (keyEvent.getKeyCode()){
      case VK_UP, VK_SPACE -> jump();
    }
  }




  public void jump(){
    if (player().velocity().y == 0) {
      player = changeAnimation(jumpingpic, player.pos(), player.velocity());
      player.jump();
      if (jumpingsound.isRunning()) {
        jumpingsound.stop();
      }
      jumpingsound.setFramePosition(0);
      jumpingsound.start();
    }
  }
	@Override
	public boolean won() {
		return false;
	}

	@Override
	public boolean lost() {
		return false;
	}

    @Override
    public GameObj player(){
      return player;
    }


  @Override
  public int height(){
    return height;
  }

  @Override
  public int width(){
    return width;
  }


  @Override
  public List<List<? extends GameObj>> goss (){
    return goss;
  }




  public static void main(String... args){
    new JungleJumper().play();
  }


  public FallingImage changeAnimation(String imageFileName,Vertex corner, Vertex movement) {
    FallingImage copy = new FallingImage(imageFileName, player.pos(), player.velocity());
    return copy;
  }


  private void initBackgroundMusic() {
    try {
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("eren/sound/jungle.wav"));
      music = AudioSystem.getClip();
      music.open(audioInputStream);
      music.loop(Clip.LOOP_CONTINUOUSLY);
      FloatControl gainControl = (FloatControl) music.getControl(FloatControl.Type.MASTER_GAIN);
      float volume = -15.0f;
      gainControl.setValue(volume);
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      e.printStackTrace();
    }
  }

  private void initJumpingSound() {
    try {
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("eren/sound/jumpingsound.wav"));
      jumpingsound = AudioSystem.getClip();
      jumpingsound.open(audioInputStream);
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      e.printStackTrace();
    }
  }

  private void initLostSound() {
    try {
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("eren/sound/lost.wav"));
      lostsound = AudioSystem.getClip();
      lostsound.open(audioInputStream);
      FloatControl gainControl2 = (FloatControl) lostsound.getControl(FloatControl.Type.MASTER_GAIN);
      float volume2 = -20.0f;
      gainControl2.setValue(volume2);
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      e.printStackTrace();
    }
  }

  private void initDamageSound() {
    try {
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("eren/sound/damagesound.wav"));
      damagesound = AudioSystem.getClip();
      damagesound.open(audioInputStream);
      FloatControl gainControl3 = (FloatControl) damagesound.getControl(FloatControl.Type.MASTER_GAIN);
      float volume = 6.0f;
      gainControl3.setValue(volume);
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      e.printStackTrace();
    }
  }

}

