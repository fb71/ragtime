/**
 */
window.addLongPressListener = function( elm, callback) {
    //window.console.log( "addLongPressListener: " + elm.className );
    let timer;

    let onStart = (ev) => {
        window.console.log( "onStart" );
        ev.stopPropagation();
        ev.preventDefault();
        ev.stopImmediatePropagation(); // no hover, no click, no nothing
        
        timer = setTimeout( () => {
            window.console.log( "timeout 500" );
            timer = null;
            callback( ev );
        }, 500 );
        window.console.log( "timer: " + timer );
    };
    elm.addEventListener( 'touchstart', onStart );
    elm.addEventListener( 'mousedown', onStart );

    let onEnd = (ev) => {
        window.console.log( "onEnd" );
        if (timer) {
            window.console.log( "element.click(): " + ev.target );
            const event = new MouseEvent( "click", {
                view: window,
                bubbles: false,
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
