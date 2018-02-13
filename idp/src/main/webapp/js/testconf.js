jQuery(function($) {
  
  var servletUrl = "/idp/extauth"
  
  var selectedUser;
  
  // UI texts
  var notSelectedText = $('#notSelectedText').html();
  var activeYesText = $('#activeYesText').html();
  var activeNoText = $('#activeNoText').html();
  
  $(document).ready(function () {
    
    // If no radio button is selected, we disable the save button.
    if ($('#testuserDiv input:radio:checked').length) {
      $('#saveButton').prop('disabled', false);
    }
    else {
      $('#saveButton').prop('disabled', true);
    }
  
    $('#testuserDiv input:radio').click(function() {
      selectedUser = this.value;
      $('#saveButton').prop('disabled', false);      
    });
    
    $('#saveButton').click(function(e) {
      e.preventDefault();
      $.post(servletUrl + '/autoauth/save', { selectedUser, selectedUser });
      updateCurrentSetting();
      return false;
    });
    
    $('#eraseButton').click(function(e) {
      selectedUser = null;
      $('#testuserDiv input:radio:checked').each(function () { 
        $(this).prop('checked', false); 
      });
      updateCurrentSetting();
      $.post(servletUrl + '/autoauth/reset');
      return false;
    });
  
  });
  
  function updateCurrentSetting() {
    if (selectedUser != null) {
      $('#autoAuthActiveFlag').html(activeYesText);
      $('#selectedUserId').html(selectedUser);
    }
    else {
      $('#autoAuthActiveFlag').html(activeNoText);
      $('#selectedUserId').html(notSelectedText);
    }
  }

});