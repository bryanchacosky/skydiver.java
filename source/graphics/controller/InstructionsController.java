package com.bryanchacosky.core.graphics.controller;

import playn.core.PlayN;
import playn.core.Pointer.Event;
import playn.core.Pointer.Listener;
import playn.core.gl.ImageLayerGL;

import com.bryanchacosky.core.SkyDiver;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Controller to display the game instructions to the user.
 *
 * @author Bryan Chacosky
 */
public class InstructionsController extends ImageLayerGL implements com.bryanchacosky.core.graphics.Layer
{
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Instantiates a default instructions controller.
   */
  public InstructionsController( )
  {
    super( PlayN.graphics( ).ctx( ) );
    super.setSize( SkyDiver.WindowWidth, SkyDiver.WindowHeight );
    super.setImage( PlayN.assets( ).getImage( "images/instructions.png" ) );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void onAdd( )
  {
    super.onAdd( );

    // Set the pointer:
    PlayN.pointer( ).setListener( new Listener( )
    {
      @Override
      public void onPointerStart( final Event event )
      {
      }

      @Override
      public void onPointerEnd( final Event event )
      {
        // Start a new round:
        SkyDiver.setContentLayer( new GameController( ) );
      }

      @Override
      public void onPointerDrag( final Event event )
      {
      }
    });
  }

  @Override
  public void update( final float delta )
  {
  }

  @Override
  public void paint( final float alpha )
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////