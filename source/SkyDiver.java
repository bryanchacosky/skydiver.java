package com.bryanchacosky.core;

import playn.core.Game;
import playn.core.Layer;
import playn.core.PlayN;

import com.bryanchacosky.core.graphics.controller.MenuController;
import com.bryanchacosky.core.graphics.layer.BackgroundLayer;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Game instance for SkyDiver.
 *
 * @author Bryan Chacosky
 */
public class SkyDiver implements Game
{
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Rate to update each frame, in milliseconds. */
  public static final int UpdateRate = 25;

  /** Static width of the window. */
  public static final int WindowWidth  = ( int )( PlayN.graphics( ).screenWidth( ) * 0.75 );

  /** Static height of the window. */
  public static final int WindowHeight = ( int )( PlayN.graphics( ).screenHeight( ) * 0.75 );

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Background sky layer that will persist throughout the lifespan of the application. */
  private static final BackgroundLayer backgroundLayer = new BackgroundLayer( );

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void init( )
  {
    // Initialize the window properties:
    PlayN.graphics( ).setSize( SkyDiver.WindowWidth, SkyDiver.WindowHeight );
    System.out.println( "Initializing window: " + SkyDiver.WindowWidth + ", " + SkyDiver.WindowHeight );

    // Kick off the game by displaying the menu controller:
    SkyDiver.setContentLayer( new MenuController( ) );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void paint( final float alpha )
  {
    assert PlayN.graphics( ).rootLayer( ).size( ) == 2 : "Must have only two root layers: background + content!";

    // Re-paint the background layer:
    SkyDiver.backgroundLayer.paint( alpha );

    // Pull the custom layer instance from the content layer:
    final Layer layer = PlayN.graphics( ).rootLayer( ).get( 1 );
    assert layer instanceof com.bryanchacosky.core.graphics.Layer == false : "Content layer must be instance of com.bryanchacosky.core.graphics.Layer!";
    final com.bryanchacosky.core.graphics.Layer customLayer = com.bryanchacosky.core.graphics.Layer.class.cast( layer );

    // Delegate the method to the current content layer:
    customLayer.paint( alpha );
  }

  @Override
  public void update( final float delta )
  {
    assert PlayN.graphics( ).rootLayer( ).size( ) == 2 : "Must have only two root layers: background + content!";

    // Re-paint the background layer:
    SkyDiver.backgroundLayer.update( delta );

    // Pull the custom layer instance from the content layer:
    final Layer layer = PlayN.graphics( ).rootLayer( ).get( 1 );
    assert layer instanceof com.bryanchacosky.core.graphics.Layer == false : "Content layer must be instance of com.bryanchacosky.core.graphics.Layer!";
    final com.bryanchacosky.core.graphics.Layer customLayer = com.bryanchacosky.core.graphics.Layer.class.cast( layer );

    // Delegate the method to the current content layer:
    customLayer.update( delta );
  }

  @Override
  public int updateRate( )
  {
    return SkyDiver.UpdateRate;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Sets the current content layer to display to the user.  Previous layer(s)
   * will be cleared from the render stack.
   *
   * @param layer - New content layer.
   */
  public static void setContentLayer( final com.bryanchacosky.core.graphics.Layer layer )
  {
    assert layer != null : "Cannot set a null content layer!";

    // Clear any existing pointer listeners:
    PlayN.pointer( ).setListener( null );

    // Remove all current layers, and replace with the passed layer:
    PlayN.graphics( ).rootLayer( ).clear( );
    PlayN.graphics( ).rootLayer( ).add( SkyDiver.backgroundLayer );
    PlayN.graphics( ).rootLayer( ).add( layer );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////