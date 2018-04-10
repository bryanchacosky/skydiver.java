package com.bryanchacosky.core.graphics.controller;

import java.util.Timer;
import java.util.TimerTask;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.Mouse;
import playn.core.Mouse.ButtonEvent;
import playn.core.Mouse.MotionEvent;
import playn.core.Mouse.WheelEvent;
import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.Pointer.Event;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GroupLayerGL;

import com.bryanchacosky.core.SkyDiver;
import com.bryanchacosky.core.utilities.Animator;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Controller presenting the user with the game options.
 *
 * @author Bryan Chacosky
 */
public class MenuController extends GroupLayerGL implements com.bryanchacosky.core.graphics.Layer
{
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Layer to display the game title. */
  private final Layer.HasSize titleLayer;

  /** Layer which holds each of the options. */
  private final GroupLayer optionsLayer;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Instantiates the default menu layer.
   */
  public MenuController( )
  {
    super( PlayN.graphics( ).ctx( ) );

    // Initialize the final layers:
    super.add( this.titleLayer = this.createTitleLayer( ) );
    super.add( this.optionsLayer = PlayN.graphics( ).createGroupLayer( ) );

    // Create a group layer to hold each of the options:
    this.addOption( "Play", new Pointer.Adapter( )
    {
      @Override
      public void onPointerEnd( final Event event )
      {
        SkyDiver.setContentLayer( new GameController( ) );
      }
    });
    this.addOption( "Instructions", new Pointer.Adapter( )
    {
      @Override
      public void onPointerEnd( final Event event )
      {
        SkyDiver.setContentLayer( new InstructionsController( ) );
      }
    });
    this.addOption( "Quit", new Pointer.Adapter( )
    {
      @Override
      public void onPointerEnd( final Event event )
      {
        System.exit( 0 );
      }
    });
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void update( final float delta )
  {
  }

  @Override
  public void paint( final float alpha )
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void onAdd( )
  {
    super.onAdd( );

    // Prepare to animate the title layer from above:
    this.titleLayer.setTranslation( ( SkyDiver.WindowWidth - this.titleLayer.width( ) ) / 2, -this.titleLayer.height( ) );
    this.optionsLayer.setVisible( false );

    // Delay the animation by a second:
    new Timer( ).schedule( new TimerTask( )
    {
      @Override
      public void run( )
      {
        // Animate the title layer down to the proper position, directly on top of the options layer:
        final float x = MenuController.this.titleLayer.transform( ).tx( );
        final float y = MenuController.this.titleLayer.height( ) / 2;
        Animator.lerp( MenuController.this.titleLayer, x, y, 1000, new Animator.Callback( )
        {
          @Override
          public void onAnimationComplete( )
          {
            /*
             * I'm pretty confused about this part where I position the options layer.  It appears that the titleLayer
             * position directly affects the optionsLayer position when they're placed within the same parent layer.  For instance,
             * I believe the correct y position should be the 'ty' value of the titleLayer + the height of the title layer to position
             * the optionsLayer directly below the title ... but instead it renders much lower.  I couldn't find much about
             * this in the documentation so I'm going to leave in what works and assume that there is some type of relative positioning
             * being done internally.
             */

            // Animation complete, so present the options:
            final float x = ( SkyDiver.WindowWidth - MenuController.width( MenuController.this.optionsLayer ) ) / 2;
            final float y = MenuController.this.titleLayer.transform( ).ty( ) + MenuController.this.titleLayer.height( ) / 2;
            MenuController.this.optionsLayer.setTranslation( x, y );
            MenuController.this.optionsLayer.setVisible( true );
          }
        });
      }
    }, 1000 );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Creates the title layer.
   *
   * @return Title layer.
   */
  private Layer.HasSize createTitleLayer( )
  {
    return PlayN.graphics( ).createImageLayer( PlayN.assets( ).getImage( "images/title.png" ) );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Adds a menu option.
   *
   * @param option - String to display for the option.
   * @param onClick - Click handler.
   */
  private void addOption( final String option, final Pointer.Adapter onClick )
  {
    // Create the text layout:
    final TextLayout layout = PlayN.graphics( ).layoutText( option, new TextFormat( ).withFont( PlayN.graphics( ).createFont( "Helvetica", Font.Style.PLAIN, 85.0f ) ) );

    // Wrap the layout within an image:
    final CanvasImage image = PlayN.graphics( ).createImage( ( int )layout.width( ),( int )layout.height( ) );
    image.canvas( ).setFillColor( 0xFF404040 );
    image.canvas( ).fillText( layout, 0, 0 );

    // Wrap the image into an image layer:
    final ImageLayer layer = PlayN.graphics( ).createImageLayer( image );
    layer.addListener( onClick );
    layer.addListener( new Mouse.LayerListener( )
    {
      @Override
      public void onMouseWheelScroll( final WheelEvent event )
      {
      }

      @Override
      public void onMouseUp( final ButtonEvent event )
      {
      }

      @Override
      public void onMouseOver( final MotionEvent event )
      {
        // Custom mouse over settings:
        image.canvas( ).clear( );
        image.canvas( ).setFillColor( 0xFFFFA465 );
        image.canvas( ).fillText( layout, 0, 0 );
      }

      @Override
      public void onMouseOut( final MotionEvent event )
      {
        // Restore default settings:
        image.canvas( ).clear( );
        image.canvas( ).setFillColor( 0xFF404040 );
        image.canvas( ).fillText( layout, 0, 0 );
      }

      @Override
      public void onMouseDrag( final MotionEvent event )
      {
      }

      @Override
      public void onMouseDown( final ButtonEvent event )
      {
      }
    });
    this.optionsLayer.add( layer );

    // Assume that each option will not wrap, so they will all have the same height:
    layer.setTranslation( 0, this.optionsLayer.size( ) * layout.height( ) );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the width of a group layer based off the layer's contents.
   *
   * @param layer - Group layer.
   * @return Width of the layer.
   */
  private static float width( final GroupLayer layer )
  {
    float width = Float.MIN_VALUE;

    for ( int i = 0; i != layer.size( ); ++i )
    {
      final Layer sublayer = layer.get( i );
      assert sublayer instanceof Layer.HasSize == false : "Sublayer must be a subclass of Layer.HasSize to find the width!";
      final Layer.HasSize sublayerWithSize = Layer.HasSize.class.cast( sublayer );

      width = Math.max( width, sublayerWithSize.width( ) );
    }

    return width;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////