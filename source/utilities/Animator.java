package com.bryanchacosky.core.utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.Layer;

import com.bryanchacosky.core.SkyDiver;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Abstract class containing static methods to assist with animation.
 *
 * @author Bryan Chacosky
 */
public abstract class Animator
{
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** When setting a looping animation, this value will loop the animation forever. */
  public static final int LoopsInfinite = -1;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Animation callback.
   */
  public static interface Callback
  {
    /**
     * This method is called when an animation is completed.
     */
    public void onAnimationComplete( );
  };

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Linearly interpolates a layer from its current position to the new desired position (x,y) over
   * a fixed duration.
   *
   * @param layer - Layer to interpolate.
   * @param x - Final x position.
   * @param y - Final y position.
   * @param duration - Duration of the interpolation, in milliseconds.
   */
  public static void lerp( final Layer layer, final float x, final float y, final long duration )
  {
    Animator.lerp( layer, x, y, duration, null );
  }

  /**
   * Linearly interpolates a layer from its current position to the new desired position (x,y) over
   * a fixed duration.  On completion of the interpolation, the callback will be called.
   *
   * @param layer - Layer to interpolate.
   * @param x - Final x position.
   * @param y - Final y position.
   * @param duration - Duration of the interpolation, in milliseconds.
   * @param callback - Callback when the interpolation is complete.
   */
  public static void lerp( final Layer layer, final float x, final float y, final long duration, final Callback callback )
  {
    assert layer != null : "Cannot interpolate a null layer!";
    assert duration >= 0 : "Cannot have a negative duration!";

    // Calculate the update rate based off the distance, duration, and update rate:
    final float dx = ( ( x - layer.transform( ).tx( ) ) / duration ) * SkyDiver.UpdateRate;
    final float dy = ( ( y - layer.transform( ).ty( ) ) / duration ) * SkyDiver.UpdateRate;

    // Schedule a timer to update the position incrementally each update:
    final Timer updateTimer = new Timer( );
    updateTimer.scheduleAtFixedRate( new TimerTask( )
    {
      @Override
      public void run( )
      {
        layer.setTranslation( layer.transform( ).tx( ) + dx, layer.transform( ).ty( ) + dy );
      }
    }, 0, SkyDiver.UpdateRate );

    // Schedule a timer to execute after the total duration:
    new Timer( ).schedule( new TimerTask( )
    {
      @Override
      public void run( )
      {
        // Cancel the updates:
        updateTimer.cancel( );

        // Ensure that we're at the correct final location:
        layer.setTranslation( x, y );

        // Call the callback, if applicable:
        if ( null != callback )
          callback.onAnimationComplete( );
      }
    }, duration );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Maps a layer -> timer controlling scrolling. */
  private static Map< Layer.HasSize, Timer > ScrollingTimerMap = new HashMap< Layer.HasSize, Timer >( );

  /**
   * Begins scrolling the layer across the screen from left-to-right and wrapping when applicable.
   *
   * @param layer - Layer.
   * @param duration - Duration the layer should take to traverse one screen width, in milliseconds.
   */
  public static void setScrolling( final Layer.HasSize layer, final long duration )
  {
    Animator.setScrolling( layer, duration, null );
  }

  /**
   *  Begins scrolling the layer across the screen from left-to-right and wrapping when necessary.
   *
   * @param layer - Layer.
   * @param duration - Duration the layer should take to traverse one screen width, in milliseconds.
   * @param callback - Callback is called when the layer moves offscreen and wraps.
   */
  public static void setScrolling( final Layer.HasSize layer, final long duration, final Callback callback )
  {
    assert layer != null : "Cannot scroll a null layer!";
    assert duration >= 0 : "Cannot have a negative duration!";

    // Clear existing scrolling:
    Animator.clearScrolling( layer );

    // Calculate the update rate based off the distance, duration, and update rate:
    final float dx = ( SkyDiver.WindowWidth / Float.valueOf( duration ) ) * SkyDiver.UpdateRate;

    // Schedule a timer to update the layer:
    final Timer timer = new Timer( );
    timer.scheduleAtFixedRate( new TimerTask( )
    {
      @Override
      public void run( )
      {
        // Update the translation:
        layer.setTranslation( layer.transform( ).tx( ) + dx, layer.transform( ).ty( ) );

        // If the layer is offscreen to the right, then place it back on the left offscreen:
        if ( layer.transform( ).tx( ) > SkyDiver.WindowWidth )
        {
          // Translate the layer back to the left side of the screen:
          layer.setTranslation( -layer.width( ), layer.transform( ).ty( ) );

          // Callback, if applicable:
          if ( null != callback )
            callback.onAnimationComplete( );
        }
      }
    }, 0, SkyDiver.UpdateRate );
    Animator.ScrollingTimerMap.put( layer, timer );
  }

  /**
   * Removes scrolling from a layer.
   *
   * @param layer - The layer.
   */
  public static void clearScrolling( final Layer.HasSize layer )
  {
    if ( Animator.ScrollingTimerMap.containsKey( layer ) == true )
    {
      Animator.ScrollingTimerMap.get( layer ).cancel( );
      Animator.ScrollingTimerMap.remove( layer );
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Maps an image layer -> list of animation timers associated with the layer. */
  private static Map< ImageLayer, List< Timer > > AnimationTimerMap = new HashMap< ImageLayer, List< Timer > >( );

  /**
   * Sets an animation on an image layer that will loop indefinitely.
   *
   * @param layer - Animation layer.
   * @param durationPerFrame - Duration of each frame in the animation, in milliseconds.
   * @param images - List of images to display in the animation.
   */
  public static void setAnimation( final ImageLayer layer, final long durationPerFrame, final Image ... images )
  {
    Animator.setAnimation( layer, durationPerFrame, Arrays.asList( images ) );
  }

  /**
   * Sets an animation on an image layer that will loop indefinitely.
   *
   * @param layer - Animation layer.
   * @param durationPerFrame - Duration of each frame in the animation, in milliseconds.
   * @param images - List of images to display in the animation.
   */
  public static void setAnimation( final ImageLayer layer, final long durationPerFrame, final List< Image > images )
  {
    Animator.setAnimation( layer, durationPerFrame, images, Animator.LoopsInfinite, null );
  }

  /**
   * Sets an animation on an image layer that loops a specified number of times.
   *
   * @param layer - Animation layer.
   * @param durationPerFrame - Duration of each frame in the animation, in milliseconds.
   * @param images - List of images to display in the animation.
   * @param loops - Numer of times the animation should loop.  Use {@link #LoopsInfinite} for infinite loops.
   * @param callback - Animation callback when the loops are complete.
   */
  public static void setAnimation( final ImageLayer layer, final long durationPerFrame, final List< Image > images, final int loops, final Callback callback )
  {
    assert layer != null : "Cannot set an animation on a null layer!";
    assert images.isEmpty( ) == false : "Cannot set an animation with 0 images!";
    assert durationPerFrame > 0 : "Cannot set a negative or zero duration per frame!";

    // Clear an existing animation:
    Animator.clearAnimation( layer );

    // Create an atomic integer to keep track of the current frame:
    final AtomicInteger index = new AtomicInteger( 0 );

    // Schedule a timer to change frames each duration:
    final Timer timer = new Timer( );
    timer.scheduleAtFixedRate( new TimerTask( )
    {
      @Override
      public void run( )
      {
        // Update the image and layer size:
        try
        {
          final Image image = images.get( index.get( ) );
          layer.setImage( image );
          layer.setSize( image.width( ), image.height( ) );
        }
        catch ( final NullPointerException exception )
        {
          /*
           * Image swapping occasionally calls NullPointerException within the OpenGL stack
           * since it's trying to update while rendering!  Catch the exception and hope the
           * next frame picks up properly.
           */
        }

        // Move to the next frame:
        index.set( ( index.get( ) + 1 ) % images.size( ) );
      }
    }, 0, durationPerFrame );

    // Register the timer:
    Animator.AnimationTimerMap.put( layer, new LinkedList< Timer >( ) );
    Animator.AnimationTimerMap.get( layer ).add( timer );

    // Check if we have a specified number of loops:
    if ( Animator.LoopsInfinite != loops )
    {
      assert loops >= 0 : "Loops must be infinite or a non-zero, positive value!";

      // Schedule a timer to cancel the animation:
      final Timer loopTimer = new Timer( );
      loopTimer.schedule( new TimerTask( )
      {
        @Override
        public void run( )
        {
          // Clear the animations:
          Animator.clearAnimation( layer );

          // Callback, if applicable:
          if ( null != callback )
            callback.onAnimationComplete( );
        }
      }, durationPerFrame * images.size( ) * loops );

      // Register the timer:
      Animator.AnimationTimerMap.get( layer ).add( loopTimer );
    }
  }

  /**
   * Removes any animations on a layer.
   *
   * @param layer - Animating layer.
   */
  public static void clearAnimation( final ImageLayer layer )
  {
    if ( Animator.AnimationTimerMap.containsKey( layer ) == true )
    {
      // Cancel the existing timers:
      for ( final Timer timer : Animator.AnimationTimerMap.get( layer ) )
        timer.cancel( );

      // Remove the key/value:
      Animator.AnimationTimerMap.remove( layer );
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////