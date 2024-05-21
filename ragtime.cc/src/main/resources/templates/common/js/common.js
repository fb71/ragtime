/**
 */
window.addLongPressListener = function( elm, callback) {
    //window.console.log( "addLongPressListener: " + elm.className );
    let timer;

    let onStart = (ev) => {
        log( "onStart" );
        ev.stopPropagation();
        ev.preventDefault();
        ev.stopImmediatePropagation(); // no hover, no click, no nothing
        
        timer = setTimeout( () => {
            log( "timeout 500" );
            timer = null;
            callback( ev );
        }, 500 );
        log( "timer: " + timer );
    };
    elm.addEventListener( 'touchstart', onStart );
    elm.addEventListener( 'mousedown', onStart );

    let onEnd = (ev) => {
        log( "onEnd" );
        if (timer) {
            log( "element.click(): " + ev.target );
            const event = new MouseEvent( "click", {
                view: window,
                bubbles: true,
                cancelable: true,
            });
            ev.target.dispatchEvent( event );
        }
        clearTimeout( timer );
    };
    elm.addEventListener( 'touchend', onEnd );
    elm.addEventListener( 'mouseup', onEnd );
    
    // to sensitive on my phone
//    elm.addEventListener( 'touchmove', () => {
//        window.console.log( "touchMove..." );        
//        clearTimeout( longPressTimer );
//    });
}


function log( msg ) {
    if (true ) {
        window.console.log( "LongPress: " + msg );
    }    
}