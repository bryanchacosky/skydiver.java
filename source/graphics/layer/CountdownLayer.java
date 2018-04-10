package com.bryanchacosky.core.graphics.layer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.ImageLayer;
import playn.core.PlayN;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GroupLayerGL;

import com.bryanchacosky.core.SkyDiver;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Layer containing a countdown timer.  The timer will count down from the initialized time
 * and present an visual indicator for each second that ticks.
 *
 * @author Bryan Chacosky
 */
public class CountdownLayer extends GroupLayerGL
{
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Callback interface.
   */
  public interface Callback
  {
    /**
     * This method is called when the countdown completes.
     */
    public void onCompletion( );
  };

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Instantiates a new countdown layer.
   *
   * @param duration - Duration in seconds to countdown.
   * @param callback - Callback.
   */
  public CountdownLayer( final int duration, final Callback callback )
  {
    super( PlayN.graphics( ).ctx( ) );

    // Use an atomic integer since we need a final, mutable variable:
    final AtomicInteger remainingTime = new AtomicInteger( duration );

    // Register a timer to update the countdown every second:
    final Timer presentationTimer = new Timer( );
    presentationTimer.scheduleAtFixedRate( new TimerTask( )
    {
      @Override
      public void run( )
      {
        if ( remainingTime.get( ) == 0 )
        {
          // Timer is complete:
          callback.onCompletion( );

          // Cancel the timer:
          this.cancel( );

          /*
           * I had been using 'CountdownLayer.this.destroy( );' instead of setting invisible but this
           * cause inconsistent issues with destroying the layer, since it would occassionally would
           * destroy the layer while OpenGL is painting ... so I would get concurrent modification
           * exceptions.  Making invisible isn't the best solution but it's more reliable.
           */
          CountdownLayer.this.setVisible( false );
        }
        else
        {
          // Update the countdown and decrement the duration:
          CountdownLayer.this.setCountdown( remainingTime.get( ) );
          remainingTime.set( remainingTime.get( ) - 1 );
        }
      }
    }, 0, 1000 );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Sets the countdown display.
   *
   * @param remainingTime - Number to display.
   */
  private void setCountdown( final int remainingTime )
  {
    // Create the text layout:
    final TextLayout layout = PlayN.graphics( ).layoutText( String.valueOf( remainingTime ), new TextFormat( ).withFont( PlayN.graphics( ).createFont( "Helvetica", Font.Style.PLAIN, SkyDiver.WindowHeight * 0.4f ) ) );

    // Wrap the layout within an image:
    final CanvasImage image = PlayN.graphics( ).createImage( ( int )layout.width( ),( int )layout.height( ) );
    image.canvas( ).setFillColor( 0xFFFFFFFF );
    image.canvas( ).fillText( layout, 0, 0 );
    image.canvas( ).setStrokeColor( 0xFF404040 );
    image.canvas( ).setStrokeWidth( 2.0f );
    image.canvas( ).strokeText( layout, 0, 0 );

    // Wrap the image into a PlayN layer:
    final ImageLayer layer = PlayN.graphics( ).createImageLayer( image );
    layer.setTranslation( ( SkyDiver.WindowWidth - image.width( ) ) / 2, ( SkyDiver.WindowHeight - image.height( ) ) / 2 );

    // Reset the group layer contents:
    super.clear( );
    super.add( layer );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////