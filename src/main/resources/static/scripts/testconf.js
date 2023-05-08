jQuery(function($) {
      
  $(document).ready(function () {
    
    $('#saveButton').attr('disabled', 'disabled');
    
    if ($('#selectSimulatedUser').val() == 'NONE') {
      $('#eraseButton').attr('disabled', 'disabled');
    }
    
    $('#selectSimulatedUser').change(function() {
      if ($(this).val() == 'NONE') {
        $('#saveButton').attr('disabled', 'disabled');
      }
      else {
        $('#saveButton').removeAttr('disabled');
      }
    });

  });
  
});