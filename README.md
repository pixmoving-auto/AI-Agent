<div align="center" style="text-align: center;">

<h1>PixKnow</h1>

<p>
  <b>xxx is an open-source in-vehicle voice interaction platform with L2+ autonomous driving capabilities on which you can add your personal interaction functions.</b>
  <br>
  Currently, we have a AI audio agent on it.
</p>

<h3>
  <a href="">Docs</a>
  <span> · </span>
  <a href="">Roadmap</a>
  <span> · </span>
  <a href="">Contribute</a>
  <span> · </span>
  <a href="">Community</a>
  <span> · </span>
  <a href="">Try it on PixKnow</a>
</h3>

Quick start: `bash <(curl -fsSL openpilot.comma.ai)`

![tests]()
[![codecov](https://codecov.io/gh/commaai/openpilot/branch/master/graph/badge.svg)](https://codecov.io/gh/commaai/openpilot)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

</div>

<div align="center">
<table>
  <tr>
    <td><a href="https://www.bilibili.com/video/BV1Gf4y1H7q8/?spm_id_from=333.337.search-card.all.click&vd_source=73059ada6be12ad361901d4e5660263d" title="Video By Greer Viau"><img src="https://i0.hdslb.com/bfs/archive/a44d33eaf935e2c089052b19ae93299782ecee14.jpg@672w_378h_1c_!web-search-common-cover"></a></td>

  </tr>
</table>
</div>


examples(如有有视频链接，可以不要这里，直接放在上面链接中)
------
* example1
* example2
* example3

Background
------

In alignment with the vision of Robo-EV, when considering the in-vehicle voice interactive system we refused to fall into a stereotyped framework: basic functions such as controlling temperature, volume, and windows. Instead, we aim to use AI agent to give Robo-EV a transformative interactive experience and make it a customized "partner" for users. Through this innovative product interactive experience, we hope that users can feel more convenience and fun in daily use.

Product Design
------

AI agent open-source in-car voice interaction system aims to become a powerful platform that provides L2+ autonomous driving capabilities, allowing developers to build upon it and create an open ecosystem. The system not only supports ADAS and DMS but also prioritizes emotional support for users, enhancing the overall experience through intelligent interaction.

## Scenario Descriptions

1. **Voice-Created Virtual Characters**:
   - Users can create virtual characters through simple voice commands, such as embodying a cartoon character to chat with children. This feature caters to temporary needs, bringing joy and interaction to passengers.

2. **Virtual Companionship**:
   - As users spend more time with the virtual character, the system will gradually adjust the character's personality and interaction style, making the relationship feel more intimate. This dynamic change enhances users' sense of belonging and engagement.

3. **Personalized Learning**:
   - The system will remember relevant user information and securely store it within the vehicle, ensuring that privacy is maintained. User preferences and habits will be automatically recognized and applied to create personalized interactions.

4. **Multimodal Dialogue**:
   - In the in-car environment, the system can combine user voice input, driving camera information, and vehicle GPS location to enable multimodal dialogue. This layered interaction will provide a more natural and intelligent communication experience.

5. **Music Generation**:
   - Based on user music preferences and the current context, the system can generate personalized AI music, enhancing the driving experience and creating an enjoyable atmosphere.

6. **Driving Assistant**:
   - The system combines vehicle sensor information to proactively remind users via voice alerts in case of anomalies, providing suggestions to ensure user safety and a smooth driving experience.

Roadmap
------

This is the roadmap for the AI agent releases.

## AI agent 1.0

AI agent 1.0 minimized the closed loop for creating virtual characters and their interactions through voice and expressions. Four preset virtual characters and a voice library, along with a visual representation of conversation history functions are provided.

## Software

### **Create avatars**
Users can customize and create their own avatars. To help achieve this, our project provides four preset characters with different personalities and speaking styles:
- **In-car assistant**: An assistant focused on providing information and solving problems.
- **Emotional companion**: An emotionally supportive presence that engages users in comforting conversations.
- **Guide**: A character that provides insights and suggestions about places of interest during the trip.
- **Language teacher**: An educational character that helps users learn and practice new languages.

### **Voice switching**
A voice library is provided, and users can choose different voice tones for the avatar to enhance the personality and charm of the avatar.

### **UI expression mapping**
We introduced a set of UI expressions corresponding to the content of the conversation, enhancing visual interaction and providing users with a more expressive interface.

### **Visual interface for conversation history**
A user-friendly interface displays conversation records, allowing users to easily review their interactions.

### **Local music playback**
Users can play local music files to add entertainment to the overall experience.


Hardware
------
Android system with touch screen
硬件解构图


To start developing
------


Contributions
------

We appreciate all contributions, whether big or small. Your participation helps us improve and grow the project! Contributors will be acknowledged in the project's documentation and release notes. If you would like to be recognized in a specific way, please let us know!

How to contribute
------

1. Fork the Repository
2. Clone Your Fork
3. Create a New Branch
4. Make Your Changes
5. Test Your Changes
6. Commit Your Changes
7. Push to Your Branch
8. Create a Pull Request

Community
------

- Issues: Check our [issues page] for existing bugs or feature requests you can help with.

### Code of Conduct
We expect all contributors to adhere to our [Code of Conduct]. Please be respectful and considerate to all community members.

License
------

Unless otherwise described, the code in this repository is licensed under the GNU GPL v3.0 License (GNU General Public License version 3.0). Please note that some modules, extensions or code herein might be otherwise licensed. This is indicated either in the root of the containing folder under a different license file, or in the respective file's header. If you have any questions, don't hesitate to get in touch with us via email.

Contacts
------

If you have any questions, suggestions or potential cooperation opportunities, please contact us:  
Email: tengfei.zhang@pixmoving.net  
GitHub: phoebeezh
