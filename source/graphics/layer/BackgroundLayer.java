package com.bryanchacosky.core.graphics.layer;

import java.util.Random;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.gl.GroupLayerGL;

import com.bryanchacosky.core.SkyDiver;
import com.bryanchacosky.core.utilities.Animator;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Background layer containing the sky and wrapping clouds.
 *
 * @author Bryan Chacosky
 */
public class BackgroundLayer extends GroupLayerGL implements com.bryanchacosky.core.graphics.Layer
{
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Instantiates a default background layer.
   */
  public BackgroundLayer( )
  {
    super( PlayN.graphics( ).ctx( ) );

    // Initialize the background gradient layer:
    super.add( this.createBackgroundGradientLayer( ) );

    // Create a few clouds in the sky:
    final Random random = new Random( );
    for ( int i = 0; i != random.nextInt( 3 ) + 3; ++i )
    {
      final long  speed = random.nextInt( 4000 ) + 8000;              // Duration to scroll the screen, between 8 and 12 seconds
      final float alpha = ( random.nextInt( 4 ) + 4 ) / 10.0f;        // Alpa transparency, between 0.4 and 0.8
      final int   x     = random.nextInt( SkyDiver.WindowWidth );     // Starting x position, between 0 and screen width
      final int   y     = random.nextInt( SkyDiver.WindowHeight );    // Starting y position, between 0 and screen height
      final int   image = random.nextInt( 2 );                        // Cloud asset image

      // Create the layer:
      final ImageLayer cloud = PlayN.graphics( ).createImageLayer( PlayN.assets( ).getImage( "images/cloud-" + image + ".png" ) );
      cloud.setAlpha( alpha );
      cloud.setTranslation( x, y );
      Animator.setScrolling( cloud, speed, new Animator.Callback( )
      {
        @Override
        public void onAnimationComplete( )
        {
          // About to wrap around the edge, so let's change to a new height to keep it interesting:
          cloud.setTranslation( cloud.transform( ).tx( ), random.nextInt( SkyDiver.WindowHeight ) );
        }
      });
      super.add( cloud );
    }
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

  /**
   * Creates the background gradient layer.
   *
   * @return Gradient layer.
   */
  private Layer createBackgroundGradientLayer( )
  {
    // Create the canvas image with the gradient:
    final CanvasImage canvasImage = PlayN.graphics( ).createImage( SkyDiver.WindowWidth, SkyDiver.WindowHeight );
    final Canvas canvas = canvasImage.canvas( );
    canvas.setFillGradient( PlayN.graphics( ).createLinearGradient( 0, SkyDiver.WindowHeight, 0, 0, new int[ ]{ 0xFF6090BF, 0xFF80B1FF }, new float[ ]{ 0.0f, 1.0f } ) );
    canvas.fillRect( 0, 0, SkyDiver.WindowWidth, SkyDiver.WindowHeight );

    // Convert the canvas image into an ImageLayer:
    final ImageLayer imageLayer = PlayN.graphics( ).createImageLayer( );
    imageLayer.setImage( canvasImage );
    return imageLayer;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////