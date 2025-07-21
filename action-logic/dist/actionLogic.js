(function (_, kotlin_kotlin) {
  'use strict';
  //region block: imports
  var protoOf = kotlin_kotlin.$_$.c;
  var initMetadataForObject = kotlin_kotlin.$_$.b;
  var defineProp = kotlin_kotlin.$_$.a;
  //endregion
  //region block: pre-declaration
  initMetadataForObject(ActionLogic, 'ActionLogic');
  //endregion
  function ActionLogic() {
  }
  protoOf(ActionLogic).buildSummary = function (file) {
    return 'Hello ' + file + '!';
  };
  var ActionLogic_instance;
  function ActionLogic_getInstance() {
    return ActionLogic_instance;
  }
  //region block: init
  ActionLogic_instance = new ActionLogic();
  //endregion
  //region block: exports
  function $jsExportAll$(_) {
    defineProp(_, 'ActionLogic', ActionLogic_getInstance);
  }
  $jsExportAll$(_);
  //endregion
  return _;
}(module.exports, require('./kotlin-kotlin-stdlib.js')));

//# sourceMappingURL=actionLogic.js.map
