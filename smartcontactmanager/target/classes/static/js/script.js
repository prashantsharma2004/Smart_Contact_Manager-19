console.log("this is script file");

const toggleSidebar = () => {
  const sidebar = document.querySelector(".sidebar");
  const content = document.querySelector(".content");

  if (sidebar.style.display === "block") {
    sidebar.style.display = "none";
    content.style.marginLeft = "0%";
  } else {
    sidebar.style.display = "block";
    content.style.marginLeft = "20%";
  }
};

// search contact 
const search=() => {
  let query = $("#search-input").val();

  if (query == "") {
    $('.search-result').hide();
   // $('.original-contact').show();
  } else {
    console.log(query);
    $(".search-result").show();
    let url = `http://localhost:8080/search/${query}`;

    // fetching data from server
    fetch(url).then((response) => {
      return response.json();
    })

    // if data is coming then
    .then((data) => {

     // console.log(data);

      let text = `<div class="list-group">`;

      data.forEach((contact) => {

        text += `<a href="/user/${contact.cId}/contact" 
        class="list-group-item list-group-item-action">${contact.name}</a>`;
      });

      text += `</div>`;

      $('.search-result').html(text);
      $('.search-result').show();
    }).catch((error) => {
      console.log(error);
    });
}
};
