/*
 * Copyright 2023-2025 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
$(document).ready(

    function() {

      // $('.drop-down-container').show();
      $('#helpsection').show();

      var selectedUser = $('#selectSimulatedUser').val();
      if (selectedUser == 'NONE') {
        $('#authenticateButton').attr('disabled', 'disabled');
        if (isAdvancedValid()) {
          $('#advancedSettings').show();
          $('#selectSimulatedUser').attr('disabled', 'disabled');
          $('#advancedButton').hide();
        }
      }
      else {
        $('#personalIdNumber').val('');
        $('#givenName').val('');
        $('#surname').val('');
      }

      // We only support "Advanced" if the agent support JS
      $('#simulateErrorButton').parent().show();

      $('#simulateErrorButton').click(function() {
        if ($('#simulateError').is(':hidden')) {
          $('#simulateError').show();
          $('#simulateErrorButton').hide();

          $('#advancedButton').hide();
          $('#submitButton').attr('disabled', 'disabled');
          $('#selectSimulatedUser').attr('disabled', 'disabled');
        }
      });

      $('#cancelErrorButton').click(function() {
        $('#simulateError').hide();
        $('#simulateErrorButton').show();

        $('#advancedButton').show();
        if (selectedUser != 'NONE') {
          $('#submitButton').removeAttr('disabled');
        }
        else {
          $('#submitButton').attr('disabled', 'disabled');
        }
        $('#selectSimulatedUser').removeAttr('disabled');
      });


      $('#advancedButton').parent().show();

      $('#advancedButton').click(function() {
        if ($('#advancedSettings').is(':hidden')) {
          $('#advancedSettings').show();
          $('#selectSimulatedUser').val("NONE");
          $('#selectSimulatedUser').attr('disabled', 'disabled');
          $('#advancedButton').hide();
          if (!isAdvancedValid()) {
            $('#submitButton').attr('disabled', 'disabled');
          }
          else {
            $('#submitButton').removeAttr('disabled');
          }
        }
      });

      $('#cancelAdvancedButton').click(function() {
        $('#advancedSettings').hide();
        $('#selectSimulatedUser').removeAttr('disabled');
        $('#advancedButton').show();
        $('#selectSimulatedUser').val(selectedUser);
        if (selectedUser != 'NONE') {
          $('#submitButton').removeAttr('disabled');
        }
        else {
          $('#submitButton').attr('disabled', 'disabled');
        }
      });

      $('#selectSimulatedUser').change(function() {
        selectedUser = $(this).val();

        if (selectedUser != 'NONE') {
          $('#submitButton').removeAttr('disabled');
        }
        else {
          $('#submitButton').attr('disabled', 'disabled');
        }
      });

      $('.drop-down > p').click(function() {
        $(this).parent('.drop-down').toggleClass('open');
      });

      $('#personalIdNumber').on("change paste keyup", function() {
        if (isAdvancedValid()) {
          $('#submitButton').removeAttr('disabled');
        }
        else {
          $('#submitButton').attr('disabled', 'disabled');
        }
      });

      $('#givenName').on("change paste keyup", function() {
        if (isAdvancedValid()) {
          $('#submitButton').removeAttr('disabled');
        }
        else {
          $('#submitButton').attr('disabled', 'disabled');
        }
      });

      $('#surname').on("change paste keyup", function() {
        if (isAdvancedValid()) {
          $('#submitButton').removeAttr('disabled');
        }
        else {
          $('#submitButton').attr('disabled', 'disabled');
        }
      });

      function isAdvancedValid() {
        var pnr = personalIdNumber();
        if (!pnr) {
          return false;
        }
        else {
          $('#personalIdNumber').val(pnr);
        }

        if ($('#givenName').val().trim() == ''
            && $('#surname').val().trim() == '') {
          for (var i = 0; i < users.length; i++) {
            if (users[i].pnr == pnr) {
              $('#givenName').val(users[i].givenName);
              $('#surname').val(users[i].surname);
              break;
            }
          }
        }
        if ($('#givenName').val().trim().length > 0
            && $('#surname').val().trim().length > 0) {
          return true;
        }
        return false;
      }

      function personalIdNumber() {
        var pnr = $('#personalIdNumber').val();
        $('#personalIdNumber').removeClass('is-invalid');

        if (pnr.length == 0) {
          return false;
        }
        else if (pnr.length < 12
            || (pnr.length == 12 && pnr.indexOf('-') != -1)) {
          return false;
        }
        else {
          var res = valfor.personalidnum(pnr, valfor.NBR_DIGITS_12);
          if (res == false) {
            $('#personalIdNumber').addClass('is-invalid');
            $('#badPersonalIdNumber').show();
            return false;
          }
          else {
            $('#personalIdNumber').removeClass('is-invalid');
            $('#badPersonalIdNumber').hide();
            return res;
          }
        }
      }

    });
