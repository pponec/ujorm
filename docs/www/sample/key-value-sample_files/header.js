if(typeof SF === 'undefined'){
  SF={};
}
SF.Popover = function($popover) {
    var klass = $popover.attr('id').replace(/-tooltip/, '');
    var $parents = $popover.parents('header');
    var isOpen = false;

    function open(e) {
        e.preventDefault();
        e.stopPropagation();
        // close other popovers
        $('.tooltip:not(.' + klass + '):visible', $parents).trigger('popover:close');
        $('.tooltip.' + klass, $popover).show();
        $('body').on('click.popover', function(e) {
            $popover.trigger('popover:close');
        });
        $(document).on('keydown.popover', function(e) {
            if ((e.which || e.keyCode) === 27) {
                e.preventDefault();
                $popover.trigger('popover:close');
            }
        });
        isOpen = true;
    }

    function close(e) {
        if(!$(e.target.parentNode).closest('div').hasClass('tooltip')){
          e.preventDefault();
          e.stopPropagation();
        }
        $parents.find('.tooltip:visible').hide();
        $('body').off('click.popover');
        $(document).off('keydown.popover');
        isOpen = false;
    }

    $popover.on('click', 'a', function(e) {
        if (isOpen) {
            close(e);
        } else {
            if(!$(e.target).hasClass('not-available')){
              open(e);
            }
        }
    });
    $popover.on('popover:close', close);
};

jQuery(function($) {
    // Setup the updater popover
    var $updater = $('#updater-tooltip');
    if ($updater.length) {
        if ($updater.hasClass('fetch')) {
            $.ajax({
                url: '/user/updates/find',
                global: false,
                success: function(data) {
                    if (data.length) {
                        $updater.hide()
                                .html(data)
                                .show();
                        SF.Popover($updater);
                    }
                }
            });
        } else {
            SF.Popover($updater);
        }
    }
    // Setup the account popover
    var $account_tip = $('#account-tooltip');
    if($account_tip.length) {
      SF.Popover($account_tip);
    }
});