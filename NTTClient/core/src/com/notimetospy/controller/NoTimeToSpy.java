package com.notimetospy.controller;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import com.notimetospy.view.*;

/**
 * Die Klasse NoTimeToSpy implementiert das Interface com.badlogic.gdx.Game, dieses Inetrface ermöglicht es zwischen evrschiedenen
 * Screen umzuschalten.
 *
 * In dieser Klasse werden die Texturen des Spiels geladen, die Screens werden gesetzt.
 * Diese Klasse behinhaltet auch die Methoden zum wechseln der Screens.
 */
public class NoTimeToSpy extends Game {

	private MainMenuScreen mainMenuScreen;
	private SettingScreen settingScreen;
	private AudioSettingScreen audioSettingScreen;
	private VideoSettingScreen videoSettingScreen;
	private LevelEditorScreen levelEditorScreen;
	private ElectionPhaseScreen electionPhaseScreen;
	private EquipmentPhaseScreen equipmentPhaseScreen;
	private GameScreen gameScreen;
	private GameMenuScreen gameMenuScreen;

	private MessageEmitter messageEmitter;
	private ConnectionHandler connectionHandler;
	public MessageReceiver messageReceiver;
	private GameHandler gameHandler;

	public static Texture background, dialogBackground;
	public static Skin skin;

	public static SpriteBatch batch;

	public static Texture bar_s, bar_t, frei, kamin, roulette, tresor, wand, bar_s_betretbar,
			bar_s_ziel, bar_t_ziel, frei_betretbar, frei_ziel, kamin_ziel, roulette_ziel, tresor_ziel,
			wand_ziel, frei_diamanthalsband, frei_diamanthalsband_ziel, frei_diamanthalsband_betretbar,
			frei_klingenhut, frei_klingenhut_betretbar, frei_klingenhut_ziel, bar_t_cocktail, bar_t_cocktail_ziel,
			verb_char, verb_char_ziel, verb_char_betretbar, feind_char, feind_char_ziel, feind_char_betretbar,
			neutral_char, neutral_char_ziel, neutral_char_betretbar, akt_char, akt_char_ziel,
			verb_char_cocktail, verb_char_cocktail_ziel, verb_char_cocktail_betretbar,
			feind_char_cocktail, feind_char_cocktail_ziel, feind_char_cocktail_betretbar,
			neutral_char_cocktail, neutral_char_cocktail_ziel, neutral_char_cocktail_betretbar,
			akt_char_cocktail, akt_char_cocktail_ziel, katze, katze_betretbar
	;

	//Methoden um den Screen zu ändern
	public void showMainMenuScreen(){
		this.setScreen(mainMenuScreen);
	}
	public void showSettingScreen(){this.setScreen(settingScreen);}
	public void showAudioSettingScreen(){this.setScreen(audioSettingScreen);}
	public void showVideoSettingScreen(){this.setScreen(videoSettingScreen);}
	public void showLevelEditorScreen(){this.setScreen(levelEditorScreen);}
	public void showElectionPhaseScreen(){this.setScreen(electionPhaseScreen);}
	public void showEquipmentPhaseScreen(){this.setScreen(equipmentPhaseScreen);}
	public void showGameScreen(){this.setScreen(gameScreen);}
	public void showGameMenuScreen(){this.setScreen(gameMenuScreen);}

	@Override
	public void create () {

		Gdx.graphics.setTitle("No Time To Spy");

		background = new Texture("casino.jpg");
		dialogBackground = new Texture("dialogBackground.png");
		skin = new Skin(Gdx.files.internal("neon/skin/neon-ui.json"));

		batch = new SpriteBatch();

		//Textures laden
		bar_t = new Texture(Gdx.files.internal("Spielfeld/Bar_Tisch.png"));
		bar_s = new Texture(Gdx.files.internal("Spielfeld/Bar_Stuhl.png"));
		frei = new Texture(Gdx.files.internal("Spielfeld/Frei.png"));
		kamin = new Texture(Gdx.files.internal("Spielfeld/Kamin.png"));
		roulette = new Texture(Gdx.files.internal("Spielfeld/Roulette_Tisch.png"));
		tresor = new Texture(Gdx.files.internal("Spielfeld/Tresor.png"));
		wand =  new Texture(Gdx.files.internal("Spielfeld/Wand_mitte.png"));

		bar_s_betretbar = new Texture(Gdx.files.internal("Spielfeld/Bar_Stuhl_betretbar.png"));
		bar_s_ziel = new Texture(Gdx.files.internal("Spielfeld/Bar_Stuhl_Ziel.png"));
		bar_t_ziel = new Texture(Gdx.files.internal("Spielfeld/Bar_Tisch_Ziel.png"));
		frei_betretbar = new Texture(Gdx.files.internal("Spielfeld/Frei_betretbar.png"));
		frei_ziel = new Texture(Gdx.files.internal("Spielfeld/Frei_Ziel.png"));
		kamin_ziel = new Texture(Gdx.files.internal("Spielfeld/Kamin_Ziel.png"));
		roulette_ziel = new Texture(Gdx.files.internal("Spielfeld/Roulette_Tisch_Ziel.png"));
		tresor_ziel = new Texture(Gdx.files.internal("Spielfeld/Tresor_Ziel.png"));
		wand_ziel = new Texture(Gdx.files.internal("Spielfeld/Wand_mitte_Ziel.png"));//nur falls jede Wand dasselbe Tile hat, sonst werden hiervon mehrere benoetigt

		frei_diamanthalsband = new Texture(Gdx.files.internal("Spielfeld/Frei_DiamantHalsband.png"));
		frei_diamanthalsband_ziel = new Texture(Gdx.files.internal("Spielfeld/Frei_DiamantHalsband_Ziel.png"));
		frei_diamanthalsband_betretbar = new Texture(Gdx.files.internal("Spielfeld/Frei_DiamantHalsband_betretbar.png"));
		frei_klingenhut = new Texture(Gdx.files.internal("Spielfeld/Frei_Klingenhut.png"));
		frei_klingenhut_ziel = new Texture(Gdx.files.internal("Spielfeld/Frei_Klingenhut_Ziel.png"));
		frei_klingenhut_betretbar = new Texture(Gdx.files.internal("Spielfeld/Frei_Klingenhut_betretbar.png"));
		bar_t_cocktail = new Texture(Gdx.files.internal("Spielfeld/Bar_Tisch_Cocktail.png"));
		bar_t_cocktail_ziel = new Texture(Gdx.files.internal("Spielfeld/Bar_Tisch_Cocktail_Ziel.png"));
		verb_char = new Texture(Gdx.files.internal("Spielfeld/verb_char.png"));
		verb_char_ziel = new Texture(Gdx.files.internal("Spielfeld/verb_char_ziel.png"));
		verb_char_betretbar = new Texture(Gdx.files.internal("Spielfeld/verb_char_betretbar.png"));
		feind_char = new Texture(Gdx.files.internal("Spielfeld/feind_char.png"));
		feind_char_ziel = new Texture(Gdx.files.internal("Spielfeld/feind_char_ziel.png"));
		feind_char_betretbar = new Texture(Gdx.files.internal("Spielfeld/feind_char_betretbar.png"));
		neutral_char = new Texture(Gdx.files.internal("Spielfeld/neutral_char.png"));
		neutral_char_ziel = new Texture(Gdx.files.internal("Spielfeld/neutral_char_ziel.png"));
		neutral_char_betretbar = new Texture(Gdx.files.internal("Spielfeld/neutral_char_betretbar.png"));
		akt_char = new Texture(Gdx.files.internal("Spielfeld/activeCharacter.png"));
		akt_char_ziel = new Texture(Gdx.files.internal("Spielfeld/activeCharacter_ziel.png"));
		//Charaktere mit Cocktails
		verb_char_cocktail = new Texture(Gdx.files.internal("Spielfeld/verb_char_cocktail.png"));
		verb_char_cocktail_ziel = new Texture(Gdx.files.internal("Spielfeld/verb_char_cocktail_ziel.png"));
		verb_char_cocktail_betretbar = new Texture(Gdx.files.internal("Spielfeld/verb_char_cocktail_betretbar.png"));
		feind_char_cocktail = new Texture(Gdx.files.internal("Spielfeld/feind_char_cocktail.png"));
		feind_char_cocktail_ziel = new Texture(Gdx.files.internal("Spielfeld/feind_char_cocktail_ziel.png"));
		feind_char_cocktail_betretbar = new Texture(Gdx.files.internal("Spielfeld/feind_char_cocktail_betretbar.png"));
		neutral_char_cocktail = new Texture(Gdx.files.internal("Spielfeld/neutral_char_cocktail.png"));
		neutral_char_cocktail_ziel = new Texture(Gdx.files.internal("Spielfeld/neutral_char_cocktail_ziel.png"));
		neutral_char_cocktail_betretbar = new Texture(Gdx.files.internal("Spielfeld/neutral_char_cocktail_betretbar.png"));
		akt_char_cocktail = new Texture(Gdx.files.internal("Spielfeld/activeCharacter_Cocktail.png"));
		akt_char_cocktail_ziel = new Texture(Gdx.files.internal("Spielfeld/activeCharacter_Cocktail_ziel.png"));

		katze = new Texture(Gdx.files.internal("Spielfeld/katze.png"));
		katze_betretbar = new Texture(Gdx.files.internal("Spielfeld/katze_betretbar.png"));

		connectionHandler = new ConnectionHandler();
		messageEmitter = new MessageEmitter();
		messageReceiver = new MessageReceiver();
		gameHandler = new GameHandler();

		mainMenuScreen = new MainMenuScreen(this, messageEmitter,connectionHandler,messageReceiver);
		settingScreen = new SettingScreen(this);
		audioSettingScreen = new AudioSettingScreen(this);
		videoSettingScreen = new VideoSettingScreen(this);
		levelEditorScreen = new LevelEditorScreen(this);
		electionPhaseScreen = new ElectionPhaseScreen(this, messageEmitter,connectionHandler, gameHandler, messageReceiver);
		equipmentPhaseScreen = new EquipmentPhaseScreen(this,messageEmitter,messageReceiver,connectionHandler, gameHandler);
		gameScreen = new GameScreen(this, messageEmitter, connectionHandler, gameHandler);
		gameMenuScreen = new GameMenuScreen(this);

		messageReceiver.setParent(this);
		messageReceiver.setConnectionHandler(connectionHandler);
		messageReceiver.setGameScreen(gameScreen);
		messageReceiver.setGameHandler(gameHandler);
		messageReceiver.setElectionPhaseScreen(electionPhaseScreen);
		messageReceiver.setEquipmentPhaseScreen(equipmentPhaseScreen);

		gameHandler.setMessageEmitter(messageEmitter);
		gameHandler.setParent(this);
		gameHandler.setElectionPhaseScreen(electionPhaseScreen);
		gameHandler.setEquipmentPhaseScreen(equipmentPhaseScreen);
		gameHandler.setGameScreen(gameScreen);

		messageEmitter.setConnectionHandler(connectionHandler);

		this.showMainMenuScreen();
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		background.dispose();
		skin.dispose();
	}
}
