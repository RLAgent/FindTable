//http://coenraets.org/blog/phonegap-tutorial/
var app = {
    renderHomeView: function() {
        var html =
                '<frameset rows="100%,*" border="0">' +
                '<frame src="http://54.186.80.240/" frameborder="0" />' +
                '<frame frameborder="0" noresize />' +
                '</frameset>'
        $('html').html(html);
        //$('.search-key').on('keyup', $.proxy(this.findByName, this));
    },

    showAlert: function (message, title) {
        if (navigator.notification) {
            navigator.notification.alert(message, null, title, 'OK');
        } else {
            alert(title ? (title + ": " + message) : message);
        }
    },

//    findByName: function() {
//        console.log('findByName');
//        this.store.findByName($('.search-key').val(), function(employees) {
//            var l = employees.length;
//            var e;
//            $('.employee-list').empty();
//            for (var i=0; i<l; i++) {
//                e = employees[i];
//                $('.employee-list').append('<li><a href="#employees/' + e.id + '">' + //e.firstName + ' ' + e.lastName + '</a></li>');
 //           }
 //       });
 //   },

    initialize: function() {
        var self = this;
        self.renderHomeView();
    //    this.store = new MemoryStore(function() {
    //        //self.showAlert('Store Initialized', 'Info');
     //       self.renderHomeView();
      //  });
        //$('.search-key').on('keyup', $.proxy(this.findByName, this));
    }

};

app.initialize();