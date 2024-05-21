/**
 */
window.addLongPressListener = function( elm, callback) {
    let timer;
    let lastTouch;
    let t2;
    let delta;

    // onStart
    let onStart = (ev) => {
        log( "onStart (timer=" + timer + ")" );
        
        // prevent cycle on touchmove/start
        if (timer != null) {
            return
        }
        if (ev.touches) {
            delta = 0;
            lastTouch = ev.touches[0];
            log( "    startTouch=" + lastTouch );
        }
        ev.stopPropagation();
        ev.preventDefault();
        ev.stopImmediatePropagation(); // no hover, no click, no nothing
        
        timer = setTimeout( () => {
            log( "timeout 500" );
            if (timer) {
                timer = null;
                callback( ev );
            }
        }, 500 );
        log( "timer: " + timer );
    };
    elm.addEventListener( 'touchstart', onStart );
    elm.addEventListener( 'mousedown', onStart );

    // onEnd
    let onEnd = (ev) => {
        log( "onEnd" );
        if (timer) {
            log( "dispatchEvent(click): " + ev.target );
            ev.target.dispatchEvent( new MouseEvent( "click", {
                view: window,
                bubbles: true,
                cancelable: true,
            }));
        }
        clearTimeout( timer );
        timer = null;
    };
    elm.addEventListener( 'touchend', onEnd );
    elm.addEventListener( 'mouseup', onEnd );
    
    // onMove
    elm.addEventListener( 'touchmove', (ev) => {
        let currentTouch = ev.changedTouches[0]
        if (lastTouch) {
            //log( "    body:" + document.body.scrollTop )
            //document.body.scrollTop -= (delta * 4)

            let d = currentTouch.clientY - lastTouch.clientY;
            delta += d;
            log( "d: " + d + ", delta: " + delta );
            
            clearTimeout( t2 );
            t2 = setTimeout(() => {
                log( "scrollBy: " + delta );
                window.scrollBy({
                    top: -delta,
                    behavior: "smooth",
                });
            }, 10 );
            
            if (timer && Math.abs( d ) > 1) {
                clearTimeout( timer );
                timer = null;            
            }
        }
        lastTouch = currentTouch        
    });
}


function log( msg ) {
    //window.console.log( "LongPress: " + msg );
}