package com.bryanchacosky.core.graphics.controller;

import java.util.Random;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.ImmediateLayer.Renderer;
import playn.core.Layer;
import playn.core.Pattern;
import playn.core.PlayN;
import playn.core.Pointer.Event;
import playn.core.Pointer.Listener;
import playn.core.Surface;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GroupLayerGL;

import com.bryanchacosky.core.SkyDiver;
import com.bryanchacosky.core.graphics.layer.CountdownLayer;
import com.bryanchacosky.core.graphics.layer.PhysicsLayer;
import com.bryanchacosky.core.utilities.Animator;
import com.bryanchacosky.core.utilities.ParticleSystem;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Controller with the actual game logic.  This controller will perform a single round
 * of SkyDiver, and on completion will present a new {@link MenuController} controller.
 *
 * @author Bryan Chacosky
 */
public class GameController extends GroupLayerGL implements com.bryanchacosky.core.graphics.Layer
{
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Height from the top of the screen that the helicopter will fly. */
  private static final int HelicopterHeight = 10;

  /** Maximum speed which the wind can blow. */
  private static final float WindSpeedMax = 150.0f;

  /** Minimum speed which the wind can blow. */
  private static final float WindSpeedMin = 25.0f;

  /** Maximum height of the ground layer. */
  private static final int GroundMaxHeight = 150;

  /** Minimum height of the ground layer */
  private static final int GroundMinHeight = 50;

  /** Maximum width of the ground layer. */
  private static final int GroundMaxWidth = 600;

  /** Minimum width of the ground layer. */
  private static final int GroundMinWidth = 200;

  /** Maximum velocity that the jumper can safely land on the ground.  Any velocity greater than this value ... and splat!
   * If the parachute is not pulled, the jumper will typically hit the ground with about 680 m/s velocity. */
  private static final float MaximumSafeVelocity = 500.0f;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Enumeration of game states that the controller can be in.  These states will act as a faux-state machine
   * through the {@link com.bryanchacosky.graphics.controller.GameController#setState(State) setState} and
   * {@link com.bryanchacosky.graphics.controller.GameController#update() update} methods.
   */
  private static enum State
  {
    /** Countdown timer is ticking. */
    Countdown,

    /** Helicopter is on the move and waiting for the player to launch. */
    PreLaunch,

    /** Player has launched and we're waiting for a success/failure response. */
    InFlight,

    /*
     * Enumeration values below represent completion states.  They are not intended to be
     * set in continuous order like the previous states.  The intention is to set a single
     * completion state based off the result of the round, and the faux-state machine
     * will handle the appropriate visual response.
     */

    /** Round is complete and the score is presented. */
    CompleteDefault,

    /** Round is complete and the splat message is presented. */
    CompleteSplat;
  };

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Ground layer for the jumper to land on. */
  private final Layer groundLayer;

  /** Helicopter layer. */
  private final ImageLayer helicopterLayer;

  /** Jumper layer. */
  private final PhysicsLayer jumperLayer;

  /** Parachute layer. */
  private final ImageLayer parachuteLayer;

  /** Wind speed. */
  private final float windspeed;

  /** Current game state. */
  private State state;

  /** Completion score. */
  private int score = 0;

  /** Randomized width of the ground layer since {@link playn.core.ImmediateLayer} doesn't provide width support. */
  private final int groundWidth;

  /** Randomized height of the ground layer since {@link playn.core.ImmediateLayer} doesn't provide height support. */
  private final int groundHeight;

  /** Time when the parachute was launched.  Used in calculating the score. */
  private double parachuteLaunchTime;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Instantiates a default game layer.
   */
  public GameController( )
  {
    super( PlayN.graphics( ).ctx( ) );

    // Randomize the ground width/height now since ImmediateLayer doesn't provide height/width:
    this.groundWidth = new Random( ).nextInt( GameController.GroundMaxWidth - GameController.GroundMinWidth ) + GameController.GroundMinWidth;
    this.groundHeight = new Random( ).nextInt( GameController.GroundMaxHeight - GameController.GroundMinHeight ) + GameController.GroundMinHeight;

    // Initialize the windspeed:
    this.windspeed = new Random( ).nextFloat( ) * ( GameController.WindSpeedMax - GameController.WindSpeedMin ) + GameController.WindSpeedMin;

    // Initialize the layers:
    super.add( this.groundLayer = this.createGroundLayer( ) );
    super.add( this.helicopterLayer = this.createHelicopterLayer( ) );
    super.add( this.jumperLayer = this.createJumperLayer( ) );
    super.add( this.parachuteLayer = this.createParachuteLayer( ) );

    // Initialize the game state:
    this.setState( State.Countdown );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Sets the current game state.
   *
   * @param state - New game state.
   */
  private void setState( final State state )
  {
    this.state = state;

    switch ( state )
    {
      case Countdown:
      {
        // Hide the gameplay layers:
        this.helicopterLayer.setVisible( false );
        this.jumperLayer.setVisible( false );
        this.parachuteLayer.setVisible( false );

        // Add a new countdown layer:
        super.add( new CountdownLayer( 3, new CountdownLayer.Callback( )
        {
          @Override
          public void onCompletion( )
          {
            GameController.this.setState( State.PreLaunch );
          }
        }));
      }
        break;

      case PreLaunch:
      {
        // Show the helicopter and jumper:
        this.helicopterLayer.setVisible( true );
        this.jumperLayer.setVisible( true );

        // Launch the helicopter:
        this.helicopterLayer.setTranslation( -this.helicopterLayer.width( ), GameController.HelicopterHeight );
        Animator.setScrolling( this.helicopterLayer, 5000, new Animator.Callback( )
        {
          @Override
          public void onAnimationComplete( )
          {
            if ( State.PreLaunch.equals( GameController.this.state ) == true )
            {
              // Helicopter went off screen while we were still in pre-launch, so terminate the game:
              GameController.this.setState( State.CompleteDefault );
            }

            // Prevent scrolling multiple times:
            Animator.clearScrolling( GameController.this.helicopterLayer );
          }
        });

        // Register a touch handler:
        PlayN.pointer( ).setListener( new Listener( )
        {
          @Override
          public void onPointerStart( final Event event )
          {
          }

          @Override
          public void onPointerEnd( final Event event )
          {
            // Make sure we're in the proper state:
            if ( State.PreLaunch.equals( state ) == true )
            {
              // Launch the little guy:
              GameController.this.setState( State.InFlight );
            }
          }

          @Override
          public void onPointerDrag( final Event event )
          {
          }
        });
      }
        break;

      case InFlight:
      {
        // Reset the vertical acceleration so he falls:
        this.jumperLayer.resetVerticalAcceleration( );

        // Set a horizontal acceleration from the wind:
        this.jumperLayer.setHorizontalAcceleration( this.windspeed );

        // Set a horizontal velocity to mimic the helicopter pulling him:
        this.jumperLayer.setHorizontalVelocity( 25.0f );

        // Register a touch handler:
        PlayN.pointer( ).setListener( new Listener( )
        {
          @Override
          public void onPointerStart( final Event event )
          {
          }

          @Override
          public void onPointerEnd( final Event event )
          {
            // Parachute opens, so clip the jumper's speed and vertical acceleration:
            GameController.this.jumperLayer.setVerticalAcceleration( PhysicsLayer.DefaultVerticalAcceleration * 0.1f );
            GameController.this.jumperLayer.setVerticalVelocity( GameController.this.jumperLayer.getVerticalVelocity( ) * 0.1f );
            GameController.this.jumperLayer.setHorizontalVelocity( GameController.this.jumperLayer.getHorizontalVelocity( ) * 0.25f );

            // Show the parachute and initialize the launch timer:
            GameController.this.parachuteLayer.setVisible( true );
            GameController.this.parachuteLaunchTime = PlayN.currentTime( );

            // Remove input listeners:
            PlayN.pointer( ).setListener( null );
          }

          @Override
          public void onPointerDrag( final Event event )
          {
          }
        });
      }
        break;

      case CompleteDefault:
      case CompleteSplat:
      {
        // Clear movement on the jumper:
        this.jumperLayer.clearMovement( );

        // Present the completion message:
        if ( State.CompleteSplat.equals( state ) == true )    super.add( GameController.createCompletionLayer( "Uh oh..." ) );
        else                                                  super.add( GameController.createCompletionLayer( "Score: " + this.score ) );

        // Register a touch handler:
        PlayN.pointer( ).setListener( new Listener( )
        {
          @Override
          public void onPointerStart( final Event event )
          {
          }

          @Override
          public void onPointerEnd( final Event event )
          {
            // Move back to the menu controller:
            SkyDiver.setContentLayer( new MenuController( ) );
          }

          @Override
          public void onPointerDrag( final Event event )
          {
          }
        });
      }
        break;
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void update( final float delta )
  {
    switch ( this.state )
    {
      case PreLaunch:
      {
        // Re-position the jumper to be aligned with the helicopter:
        final float x = this.helicopterLayer.transform( ).tx( ) + this.helicopterLayer.width( ) / 2.0f;
        final float y = this.helicopterLayer.transform( ).ty( ) + this.helicopterLayer.height( ) - this.jumperLayer.height( ) * 0.2f;
        this.jumperLayer.setTranslation( x, y );
      }
        break;

      case InFlight:
      {
        // Update the jumper layer:
        this.jumperLayer.update( delta );

        // Re-position the parachute to be aligned with the jumper:
        this.parachuteLayer.setTranslation( this.jumperLayer.transform( ).tx( ), this.jumperLayer.transform( ).ty( ) );

        // Check if the jumper is on screen:
        if ( this.jumperLayer.isOnscreen( ) == false )
        {
          // Jumper has flown out of the screen, so clear the score and finish the game:
          this.score = 0;
          GameController.this.setState( State.CompleteDefault );
        }

        // Check for a collision with the ground layer.  Use a 1 pixel height for the ground layer to make sure that we are
        // testing intersection with the top of the ground layer.  If the jumper hits the side then we shouldn't reward any
        // points:
        else if ( this.jumperLayer.intersects( this.groundLayer.transform( ).tx( ), this.groundLayer.transform( ).ty( ), this.groundWidth, 1 ) )
        {
          // Jumper has intersected with the land!  Let's check if he was going too fast and broke his legs:
          System.out.println( "Jumper landed with velocity: " + this.jumperLayer.getCurrentVelocity( ) );
          if ( this.jumperLayer.getCurrentVelocity( ) <= GameController.MaximumSafeVelocity )
          {
            // Safe jump!  Calculate the score:
            this.score  = ( int )this.windspeed;                                  // More wind, more points!
            this.score += GameController.GroundMaxWidth - this.groundWidth;       // Smaller ground, more points!
            this.score += PlayN.currentTime( ) - this.parachuteLaunchTime;        // More parachute time, more points!

            // Complete the round:
            GameController.this.setState( State.CompleteDefault );
          }
          else
          {
            // Splat...
            new ParticleSystem( )
            {
              @Override
              protected int getParticleCount( )
              {
                return new Random( ).nextInt( 5 ) + 10;
              }

              @Override
              protected int getParticleColor( )
              {
                return 0xFFFF0000;
              }

              @Override
              protected int getParticleSize( )
              {
                return new Random( ).nextInt( 2 ) + 2;
              }

              @Override
              protected long getParticleDuration( )
              {
                return new Random( ).nextInt( 500 ) + 1750;
              }
            }.fire( this, this.jumperLayer.transform( ).tx( ), this.jumperLayer.transform( ).ty( ) );

            // Complete the round:
            this.setState( State.CompleteSplat );
          }
        }
      }
        break;
    }
  }

  @Override
  public void paint( final float alpha )
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Creates the helicopter layer.
   *
   * @return The helicopter layer.
   */
  private ImageLayer createHelicopterLayer( )
  {
    final Image animationImages[ ] =
    {
      PlayN.assets( ).getImage( "images/helicopter-0.png" ),
      PlayN.assets( ).getImage( "images/helicopter-1.png" ),
      PlayN.assets( ).getImage( "images/helicopter-2.png" ),
      PlayN.assets( ).getImage( "images/helicopter-1.png" )
    };

    // Create the image layer:
    final ImageLayer layer = PlayN.graphics( ).createImageLayer( animationImages[ 0 ] );
    Animator.setAnimation( layer, 75, animationImages );
    return layer;
  }

  /**
   * Creates the jumper layer.
   *
   * @return Jumper layer.
   */
  private PhysicsLayer createJumperLayer( )
  {
    final PhysicsLayer layer = new PhysicsLayer( PlayN.assets( ).getImage( "images/jumper.png" ) );
    layer.setOrigin( layer.width( ) / 2, layer.originY( ) );
    return layer;
  }

  /**
   * Creates a randomized ground layer for the jumper to land on.
   *
   * @return Ground layer.
   */
  private Layer createGroundLayer( )
  {
    final Random random = new Random( );

    // Determine the position:
    final int x = random.nextInt( SkyDiver.WindowWidth - this.groundWidth );
    final int y = SkyDiver.WindowHeight - this.groundHeight;

    // Pull the dirt pattern:
    final Pattern pattern = PlayN.assets( ).getImage( "images/dirt.png" ).toPattern( );

    // Create an immediate layer and fill with the dirt pattern:
    final ImmediateLayer layer = PlayN.graphics( ).createImmediateLayer( this.groundWidth, this.groundHeight, new Renderer( )
    {
      @Override
      public void render( final Surface surface )
      {
        surface.setFillPattern( pattern );
        surface.fillRect( 0, 0, GameController.this.groundWidth, GameController.this.groundHeight );
      }
    });

    // Position the ground offscreen initially:
    layer.setTranslation( x, SkyDiver.WindowHeight );

    // And bring the ground back up to the proper height like some demonic platform:
    Animator.lerp( layer, x, y, 1000 );

    return layer;
  }

  /**
   * Creates the parachute layer.
   *
   * @return Parachute layer.
   */
  private ImageLayer createParachuteLayer( )
  {
    final Image image = PlayN.assets( ).getImage( "images/parachute.png" );
    final ImageLayer layer = PlayN.graphics( ).createImageLayer( image );
    layer.setSize( image.width( ), image.height( ) );
    layer.setOrigin( image.width( ) / 2, image.height( ) );
    return layer;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Creates the game over layer.
   *
   * @param message - Message to display.
   * @return Score layer.
   */
  private static Layer createCompletionLayer( final String message )
  {
    // Create the text layout:
    final TextLayout layout = PlayN.graphics( ).layoutText( message, new TextFormat( ).withFont( PlayN.graphics( ).createFont( "Helvetica", Font.Style.PLAIN, SkyDiver.WindowHeight * 0.2f ) ) );

    // Wrap the layout within an image:
    final CanvasImage image = PlayN.graphics( ).createImage( ( int )layout.width( ),( int )layout.height( ) );
    image.canvas( ).setFillColor( 0xFFFFFFFF );
    image.canvas( ).fillText( layout, 0, 0 );
    image.canvas( ).setStrokeColor( 0xFF404040 );
    image.canvas( ).setStrokeWidth( 2.0f );
    image.canvas( ).strokeText( layout, 0, 0 );

    // Wrap the image into an image layer:
    final ImageLayer layer = PlayN.graphics( ).createImageLayer( image );
    layer.setTranslation( ( SkyDiver.WindowWidth - image.width( ) ) / 2, ( SkyDiver.WindowHeight - image.height( ) ) / 2 );
    return layer;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////