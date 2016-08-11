function DocListFilter(inputId, listId, containerType){
	
	if(containerType == undefined)
		containerType = 'li'
	
	var iId = inputId;
	var lId = listId;
	
	this.init = function(){
		$(iId).change( function () {
		   filterList();
		}).keyup( function () {
		    $(this).change();
		});
	}
	
	function filterList(){
		var filterValue = $(iId).val();
		
		$(lId).each(function(){
	        $(this).find(containerType).each(function(){
	            var current = $(this);
	            current.show();
	        });
	    });
		
		if(filterValue != undefined && filterValue != ""){
			$(lId).each(function(){
		        $(this).find(containerType).each(function(){
		            var current = $(this);

		            if(current.text().toLowerCase().indexOf(filterValue.toLowerCase()) == -1)
		            	current.hide();
		            
		        });
		    });
		}
	}
}